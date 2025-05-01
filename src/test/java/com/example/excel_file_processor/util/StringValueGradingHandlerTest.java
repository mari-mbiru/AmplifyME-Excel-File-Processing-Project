package com.example.excel_file_processor.util;

import com.example.excel_file_processor.util.GradingHandler.StringValueGradingHandler;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.cellwalk.CellWalkContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringValueGradingHandlerTest {

    @Test
    void onCell_shouldPass_whenTargetValueEqualsGradingValue() {
        // Given
        WorkbookParser mockParser = mock(WorkbookParser.class);
        Cell mockCell = mock(Cell.class);
        when(mockParser.getCellValueAsString(mockCell)).thenReturn("expected");

        StringValueGradingHandlerSpy handler = new StringValueGradingHandlerSpy("expected", mockParser);

        // When
        handler.onCell(mockCell, mock(CellWalkContext.class));

        // Then
        assertTrue(handler.wasPassCalled());
        assertFalse(handler.wasFailCalled());
    }

    @Test
    void onCell_shouldFail_whenTargetValueNotEqualToGradingValue() {
        // Given
        WorkbookParser mockParser = mock(WorkbookParser.class);
        Cell mockCell = mock(Cell.class);
        when(mockParser.getCellValueAsString(mockCell)).thenReturn("unexpected");

        StringValueGradingHandlerSpy handler = new StringValueGradingHandlerSpy("expected", mockParser);

        // When
        handler.onCell(mockCell, mock(CellWalkContext.class));

        // Then
        assertTrue(handler.wasFailCalled());
        assertFalse(handler.wasPassCalled());
    }

    // Spy subclass to track pass/fail
    static class StringValueGradingHandlerSpy extends StringValueGradingHandler {
        private boolean passCalled = false;
        private boolean failCalled = false;
        private final String expectedGradingValue;

        protected StringValueGradingHandlerSpy(String expectedGradingValue, WorkbookParser parser) {
            super(mock(Sheet.class), parser);
            this.expectedGradingValue = expectedGradingValue;
        }

        @Override
        protected void pass() {
            passCalled = true;
        }

        @Override
        protected void fail() {
            failCalled = true;
        }

        @Override
        protected String getGradingCellValueFromMasterTargetCell(Cell cell) {
            return expectedGradingValue;
        }

        public boolean wasPassCalled() {
            return passCalled;
        }

        public boolean wasFailCalled() {
            return failCalled;
        }
    }
}
