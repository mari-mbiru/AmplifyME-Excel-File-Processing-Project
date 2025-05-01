package com.example.excel_file_processor.util.GradingHandler;

import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.WorkbookParser;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class GradingHandlerFactory {

    public AbstractGradingHandler createHandler(ComparisonType type, Sheet targetSheet, WorkbookParser parser) {
        return switch (type) {
            case CORRECT_VALUE, CORRECT_FORMULA -> new CorrValHandler(targetSheet, parser);
            default -> throw new IllegalArgumentException("Unsupported comparison type: " + type);
        };
    }
}
