package com.example.excel_file_processor.model;

import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.WorkbookParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GradingRowTest {
    @Mock
    WorkbookParser parser;

    @Test
    void fromRow_shouldReturnValidGradeRow_whenAllFieldsAreValid() {

        //Given
        Row row = mock(Row.class);
        Cell cell0 = mock(Cell.class);
        Cell cell1 = mock(Cell.class);
        Cell cell2 = mock(Cell.class);

        //When
        when(row.getCell(0)).thenReturn(cell0);
        when(row.getCell(1)).thenReturn(cell1);
        when(row.getCell(2)).thenReturn(cell2);

        when(parser.getCellValueAsString(cell0)).thenReturn("Sheet1");
        when(parser.getCellValueAsString(cell1)).thenReturn("A1");
        when(parser.getCellValueAsString(cell2)).thenReturn("CorrVal");

        GradingRow gradeRow = GradingRow.fromRow(row, parser);

        //Then
        assertNotNull(gradeRow);
        assertFalse(gradeRow.hasError());
        assertEquals("Sheet1", gradeRow.getSheetName());
        assertEquals("A1", gradeRow.getGradingRange());
        assertEquals(ComparisonType.CORRECT_VALUE, gradeRow.getComparisonType());
    }

    @Test
    void fromRow_shouldReturnNull_whenAllFieldsAreEmpty() {

        //Given
        Row row = mock(Row.class);
        when(row.getCell(anyInt())).thenReturn(null);

        //When
        GradingRow result = GradingRow.fromRow(row, parser);

        //Then
        assertNull(result);
    }

    @Test
    void fromRow_shouldReturnError_whenSheetNameIsMissing() {

        //Given
        Row row = mock(Row.class);
        Cell rangeCell = mock(Cell.class);
        Cell typeCell = mock(Cell.class);

        when(row.getCell(0)).thenReturn(null);
        when(row.getCell(1)).thenReturn(rangeCell);
        when(row.getCell(2)).thenReturn(typeCell);

        when(parser.getCellValueAsString(rangeCell)).thenReturn("A1");
        when(parser.getCellValueAsString(typeCell)).thenReturn("CorrVal");

        //When
        GradingRow result = GradingRow.fromRow(row, parser);

        //Then
        assertNotNull(result);
        assertTrue(result.hasError());
        assertEquals("Error Grading Value: Sheet name to grade is Blank or Empty", result.getErrorMessage());
    }

    @Test
    void fromRow_shouldReturnError_whenComparisonTypeIsInvalid() {

        //Given
        Row row = mock(Row.class);
        Cell sheetCell = mock(Cell.class);
        Cell rangeCell = mock(Cell.class);
        Cell typeCell = mock(Cell.class);

        when(row.getCell(0)).thenReturn(sheetCell);
        when(row.getCell(1)).thenReturn(rangeCell);
        when(row.getCell(2)).thenReturn(typeCell);

        when(parser.getCellValueAsString(sheetCell)).thenReturn("Sheet1");
        when(parser.getCellValueAsString(rangeCell)).thenReturn("A1");
        when(parser.getCellValueAsString(typeCell)).thenReturn("INVALID");

        //When
        GradingRow result = GradingRow.fromRow(row, parser);

        //Then
        assertNotNull(result);
        assertTrue(result.hasError());
        assertEquals("Error Grading Value: Invalid comparison type: INVALID", result.getErrorMessage());
    }

}
