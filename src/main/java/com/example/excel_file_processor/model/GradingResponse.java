package com.example.excel_file_processor.model;

import java.util.List;

public record GradingResponse(double totalScore,
                              double maxScore,
                              List<String> errors,
                              String createdAt,
                              String updatedAt,
                              double percentage) {
}
