package com.example.excel_file_processor.model;

import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.WorkbookParser;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

@Getter
public class GradingRow {
    String sheetName;
    String gradingRange;
    String rawComparisonType;
    ComparisonType comparisonType;
    String errorMessage;


    public GradingRow(String sheetName, String gradingRange, String comparisonType) {
        try {
            this.sheetName = sheetName;
            this.gradingRange = gradingRange;
            this.rawComparisonType = comparisonType;
            this.comparisonType = ComparisonType.fromString(comparisonType);
        } catch (IllegalArgumentException e) {
            this.comparisonType = null;
            this.errorMessage = "Error Grading Value: Invalid comparison type: " + comparisonType;
        }
    }

    public GradingRow(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return this.errorMessage != null;
    }

    public static GradingRow fromRow(Row row, WorkbookParser parser) {
        Cell sheetCell = row.getCell(0);
        Cell rangeCell = row.getCell(1);
        Cell typeCell = row.getCell(2);

        String sheetName = sheetCell != null ? parser.getCellValueAsString(sheetCell) : null;
        String range = rangeCell != null ? parser.getCellValueAsString(rangeCell) : null;
        String type = typeCell != null ? parser.getCellValueAsString(typeCell) : null;

        //This means the row was processed because of a non-empty cell that does not fall within the three columns that define grading.
        //Therefore, not a valid row and should be skipped/ignored.
        if ((type == null || type.isEmpty()) && (sheetName == null || sheetName.isEmpty()) && (range == null || range.isEmpty())) {
            return null;
        }

        if (sheetName == null || sheetName.isEmpty()) {
            return new GradingRow("Error Grading Value: Sheet name to grade is Blank or Empty");
        }

        if (range == null || range.isEmpty()) {
            return new GradingRow("Error Grading Value: Rage to grade is Blank or Empty");
        }

        if (type == null || type.isEmpty()) {
            return new GradingRow("Error Grading Value: Grading type to use is Blank or Empty");
        }

        return new GradingRow(sheetName, range, type);
    }

}
