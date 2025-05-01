package com.example.excel_file_processor.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkbookParserTest {

    @InjectMocks
    private WorkbookParser parser;

    @Mock
    private MultipartFile mockFile;

    @Test
    void loadWorkbook_shouldReturnWorkbook_whenValidFileAndFileNameEndsInXLSX() throws IOException {

        //Given
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        byte[] content = bos.toByteArray();

        MockMultipartFile mockExcelWorkbookFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                content
        );

        //When
        Workbook wb = parser.loadWorkBook(mockExcelWorkbookFile);

        //Then
        assertNotNull(wb);
        assertInstanceOf(XSSFWorkbook.class, wb);
    }

    @Test
    void testLoadWorkbook_shouldThrowIllegalArgumentException_whenMalFormedFile() throws IOException {

        //Given
        InputStream is = new ByteArrayInputStream(new byte[0]);
        when(mockFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(mockFile.getInputStream()).thenReturn(is);

        //When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parser.loadWorkBook(mockFile));

        //Then
        assertEquals("The uploaded file is not a valid .xlsx Excel file", ex.getMessage());
    }

    @Test
    void testLoadWorkbook_shouldThrowIllegalArgumentException_whenInvalidExtension() {

        //Given
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        //When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parser.loadWorkBook(mockFile));

        //Then
        assertEquals("Unsupported file format. Only .xlsx files are supported", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cellValueProvider")
    void testGetCellValueAsString(Cell cell, String expected) {
        assertEquals(expected, parser.getCellValueAsString(cell));
    }

    private static Stream<Arguments> cellValueProvider() {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Text");                         // STRING
            row.createCell(1).setCellValue(123.456);                        // NUMERIC
            row.createCell(2).setCellValue(true);                           // BOOLEAN
            row.createCell(3).setCellFormula("SUM(A1:A1)");                 // FORMULA
            row.createCell(4);                                              // BLANK

            // DATE cell
            Cell dateCell = row.createCell(5);
            dateCell.setCellValue(LocalDate.of(2023, 4, 30));
            CellStyle dateStyle = wb.createCellStyle();
            CreationHelper creationHelper = wb.getCreationHelper();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy"));
            dateCell.setCellStyle(dateStyle);
            String expectedDate = dateCell.getDateCellValue().toString();

            return Stream.of(
                    Arguments.of(row.getCell(0), "Text"),
                    Arguments.of(row.getCell(1), "123.456"),
                    Arguments.of(row.getCell(2), "true"),
                    Arguments.of(row.getCell(3), "SUM(A1:A1)"),
                    Arguments.of(row.getCell(4), ""),
                    Arguments.of(row.getCell(5), expectedDate),
                    Arguments.of(null, "")
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare test data", e);
        }
    }



    @ParameterizedTest
    @CsvSource({
            "B1,0,1",
            "ZA56,55,676",
            "X239,238,23"
    })
    void testGetRangeAddressFromRangeString_shouldReturnCell_whenValidSingleCellAddress(String rangeStr, int row, int col) {

        //When
        CellRangeAddress range = parser.getRangeAddressFromRangeString(rangeStr);

        //Then
        assertEquals(row, range.getFirstRow());
        assertEquals(row, range.getLastRow());
        assertEquals(col, range.getFirstColumn());
        assertEquals(col, range.getLastColumn());
    }

    @ParameterizedTest
    @CsvSource({
            "A1:A1,0,0,0,0",
            "AA1:BB20,0,19,26,53",
            "B200:D500,199,499,1,3"
    })
    void testGetRangeAddressFromRangeString_shouldReturnCellRangeAddress_whenValidCellRangeAddress(String rangeStr, int firstRow, int lastRow, int firstCol, int lastCol) {

        //When
        CellRangeAddress range = parser.getRangeAddressFromRangeString(rangeStr);

        //Then
        assertEquals(firstRow, range.getFirstRow());
        assertEquals(lastRow, range.getLastRow());
        assertEquals(firstCol, range.getFirstColumn());
        assertEquals(lastCol, range.getLastColumn());
    }

    @Test
    void testGetRangeAddressFromRangeString_shouldThrowIllegalArgumentException_whenCellRangeIsBlankOrEmpty() {
        String input = "C3:A1";
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> parser.getRangeAddressFromRangeString(input));
        assertTrue(ex.getMessage().contains("top-left to bottom-right"));
    }

    @Test
    void testGetRangeAddressFromRangeString_shouldThrowIllegalArgumentException_whenCellRangeIsBlank() {
        String input1 = " ";
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> parser.getRangeAddressFromRangeString(input1));
        assertTrue(ex.getMessage().contains("Cell range string is empty or null."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12AB", "A-1", "A1:B", "ABCD1", "1A:2B", ":B2", "A1:", "A1:20"})
    void testGetRangeAddressFromRangeString_shouldThrowIllegalArgumentException_whenMalformedRange(String input) {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> parser.getRangeAddressFromRangeString(input));

        assertTrue(ex.getMessage().contains("Invalid cell range format"));
    }
}
