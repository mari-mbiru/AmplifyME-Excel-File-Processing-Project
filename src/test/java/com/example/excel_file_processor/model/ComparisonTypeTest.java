package com.example.excel_file_processor.model;

import com.example.excel_file_processor.model.enums.ComparisonType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComparisonTypeTest {
    @ParameterizedTest
    @CsvSource({
            "CorrVal,CORRECT_VALUE",
            "corrval,CORRECT_VALUE",
            " CORRFORM ,CORRECT_FORMULA",
            "CoRrFoRm,CORRECT_FORMULA"
    })
    void fromString_shouldReturnCorrectEnum(String input, ComparisonType expected) {
        assertEquals(expected, ComparisonType.fromString(input));
    }

    @Test
    void fromString_shouldThrowException_whenInputIsUnknown() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ComparisonType.fromString("INVALID"));
        assertEquals("Unknown grading type: INVALID", ex.getMessage());
    }

    @Test
    void fromString_shouldThrowException_whenInputIsNull() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ComparisonType.fromString(null));
        assertEquals("Comparison type cannot be null", ex.getMessage());
    }

}
