package com.example.excel_file_processor.util.GradingHandler;

import com.example.excel_file_processor.util.WorkbookParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.cellwalk.CellWalkContext;

public class StringValueGradingHandler extends AbstractGradingHandler {

    protected StringValueGradingHandler(Sheet gradingSheet, WorkbookParser parser) {
        super(gradingSheet, parser);
    }

    @Override
    public void onCell(Cell cell, CellWalkContext cellWalkContext) {
        String targetValue = getParser().getCellValueAsString(cell);

        String gradingValue = getGradingCellValueFromMasterTargetCell(cell);

        if (targetValue.equals(gradingValue)) {
            pass();
        } else {
            fail();
        }
    }
}
