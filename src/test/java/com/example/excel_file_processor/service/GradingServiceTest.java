package com.example.excel_file_processor.service;

import com.example.excel_file_processor.model.GradingResponse;
import com.example.excel_file_processor.model.GradingRow;
import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.GradingHandler.GradingHandler;
import com.example.excel_file_processor.util.GradingHandler.GradingHandlerFactory;
import com.example.excel_file_processor.util.WorkbookParser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradingServiceTest {

    @Mock
    WorkbookParser workbookParser;

    @Mock
    GradingHandlerFactory gradingHandlerFactory;

    @Mock
    Workbook masterWorkbook;

    @Mock
    Workbook studentWorkbook;

    @Mock
    Sheet gradingSheet;

    @Mock
    Sheet masterSheet;

    @Mock
    Sheet studentSheet;

    @Mock
    GradingHandler gradingHandler;

    @InjectMocks
    GradingService gradingService;

    @Test
    void gradeMasterAndStudentFiles_shouldReturnGradingResponse_whenGradingSucceeds() throws Exception {
        // Given
        ComparisonType type = ComparisonType.CORRECT_VALUE;
        MultipartFile masterFile = new MockMultipartFile("master", "master.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        MultipartFile studentFile = new MockMultipartFile("student", "student.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        when(workbookParser.loadWorkBook(masterFile)).thenReturn(masterWorkbook);
        when(workbookParser.loadWorkBook(studentFile)).thenReturn(studentWorkbook);
        when(masterWorkbook.getSheet("Grading")).thenReturn(gradingSheet);

        Row mockRow = mock(Row.class);
        when(gradingSheet.rowIterator()).thenReturn(List.of(mock(Row.class), mockRow).iterator());

        GradingRow instruction = mock(GradingRow.class);
        when(instruction.hasError()).thenReturn(false);
        when(instruction.getGradingRange()).thenReturn("A1");
        when(instruction.getSheetName()).thenReturn("Sheet1");
        when(instruction.getComparisonType()).thenReturn(type);

        mockStatic(GradingRow.class).when(() -> GradingRow.fromRow(eq(mockRow), eq(workbookParser))).thenReturn(instruction);

        when(workbookParser.getRangeAddressFromRangeString("A1")).thenReturn(new CellRangeAddress(0, 0, 0, 0));
        when(masterWorkbook.getSheet("Sheet1")).thenReturn(masterSheet);
        when(studentWorkbook.getSheet("Sheet1")).thenReturn(studentSheet);

        when(gradingHandlerFactory.createHandler(type, studentSheet, workbookParser)).thenReturn(gradingHandler);
        when(gradingHandler.isPassing()).thenReturn(true);

        // When
        GradingResponse response = gradingService.gradeWorkbook(masterFile, studentFile);

        // Then
        assertEquals(1, response.totalScore());
        assertEquals(1, response.maxScore());
        assertEquals(0, response.errors().size());
    }
}

