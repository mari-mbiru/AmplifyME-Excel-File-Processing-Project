package com.example.excel_file_processor.util.GradingHandler;

import com.example.excel_file_processor.util.WorkbookParser;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.cellwalk.CellHandler;

@Getter
public abstract class GradingHandler implements CellHandler {

    private final Sheet studentSheet;
    private final WorkbookParser parser;

    private boolean passes = true;
    private String error = "";

    protected GradingHandler(Sheet studentSheet, WorkbookParser parser) {
        this.studentSheet = studentSheet;
        this.parser = parser;
    }

    protected String getGradingCellValueFromMasterCellCoords(Cell targetCell) {

        Row gradingRow = getStudentSheet().getRow(targetCell.getRowIndex());
        if (gradingRow == null) return null;

        Cell gradingCell = gradingRow.getCell(targetCell.getColumnIndex());
        if (gradingCell == null) return null;

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
