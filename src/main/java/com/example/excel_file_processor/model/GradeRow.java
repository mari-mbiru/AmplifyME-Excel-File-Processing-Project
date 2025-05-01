package com.example.excel_file_processor.model;

import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.WorkbookParser;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Objects;

@Getter
public class GradeRow {
    String sheetName;
    String gradingRange;
    String rawComparisonType;
    ComparisonType comparisonType;
    String errorMessage;


    public GradeRow(String sheetName, String gradingRange, String comparisonType) {
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

    public GradeRow(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return Objects.isNull(this.errorMessage);
    }

    public static GradeRow fromRow(Row row, WorkbookParser parser) {
        Cell sheetCell = row.getCell(0);
        Cell rangeCell = row.getCell(1);
        Cell typeCell = row.getCell(2);

        String sheetName = sheetCell != null ? parser.getCellValueAsString(sheetCell) : null;
        String range = rangeCell != null ? parser.getCellValueAsString(rangeCell) : null;
        String type = typeCell != null ? parser.getCellValueAsString(typeCell) : null;

        if ((type == null || type.isEmpty()) && (sheetName == null || sheetName.isEmpty()) && (range == null || range.isEmpty())) {
            return null;
        }

        if (sheetName == null || sheetName.isEmpty()) {
            return new GradeRow("Error Grading Value: Sheet name to grade is Blank or Empty");
        }

        if (range == null || range.isEmpty()) {
            return new GradeRow("Error Grading Value: Rage to grade is Blank or Empty");
        }

        if (type == null || type.isEmpty()) {
            return new GradeRow("Error Grading Value: Grading type to use is Blank or Empty");
        }

        return new GradeRow(sheetName, range, type);
    }

}
