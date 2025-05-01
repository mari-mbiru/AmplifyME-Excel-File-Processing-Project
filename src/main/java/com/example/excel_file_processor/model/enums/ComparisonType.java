package com.example.excel_file_processor.model.enums;


import lombok.Getter;

@Getter
public enum ComparisonType {
    CORRECT_VALUE("CorrVal"),
    CORRECT_FORMULA("CorrForm");

    private final String comparisonName;

    ComparisonType(String comparisonName) {
        this.comparisonName = comparisonName;
    }

    public static ComparisonType fromString(String value) {
        if (value == null) throw new IllegalArgumentException("Comparison type cannot be null");

        String cleaned = value.trim().toLowerCase();
        for (ComparisonType type : values()) {
            if (type.comparisonName.equalsIgnoreCase(cleaned)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown grading type: " + value);
    }

    @Override
    public String toString() {
        return comparisonName;
    }
}
