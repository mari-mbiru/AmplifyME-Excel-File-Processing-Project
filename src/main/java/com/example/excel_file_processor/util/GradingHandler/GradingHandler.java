package com.example.excel_file_processor.util.GradingHandler;

import com.example.excel_file_processor.util.WorkbookParser;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.cellwalk.CellHandler;

import java.util.Objects;

@Getter
public abstract class GradingHandler implements CellHandler {

    private final Sheet gradingSheet;
    private final WorkbookParser parser;

    private boolean passes = true;
    private String error = "";

    protected GradingHandler(Sheet gradingSheet, WorkbookParser parser) {
        this.gradingSheet = gradingSheet;
        this.parser = parser;
    }

    protected String getGradingCellValueFromMasterTargetCell(Cell targetCell) {

        Row gradingRow = getGradingSheet().getRow(targetCell.getRowIndex());
        if (Objects.isNull(gradingRow)) return null;

        Cell gradingCell = gradingRow.getCell(targetCell.getColumnIndex());
        if (Objects.isNull(gradingCell)) return null;

        return parser.getCellValueAsString(gradingCell);
    }

    protected void fail() {
        this.passes = false;
        this.error = "Values in cells do not match";
    }

    protected void pass() {
        this.passes = true;
        this.error = "";
    }

    public boolean isPassing() {
        return passes;
    }
}
