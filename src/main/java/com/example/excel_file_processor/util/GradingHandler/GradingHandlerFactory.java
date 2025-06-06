package com.example.excel_file_processor.util.GradingHandler;

import com.example.excel_file_processor.model.enums.ComparisonType;
import com.example.excel_file_processor.util.WorkbookParser;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class GradingHandlerFactory {

    public GradingHandler createHandler(ComparisonType type, Sheet targetSheet, WorkbookParser parser) {
        return switch (type) {
            case CORRECT_VALUE, CORRECT_FORMULA -> new StringValueGradingHandler(targetSheet, parser);
        };
    }
}
