package com.example.excel_file_processor.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class GradingResult {
    int totalScore;
    int maxScore;
    List<String> errors;

    public GradingResult() {
        this.totalScore = 0;
        this.maxScore = 0;
        this.errors = new ArrayList<>();
    }
}

