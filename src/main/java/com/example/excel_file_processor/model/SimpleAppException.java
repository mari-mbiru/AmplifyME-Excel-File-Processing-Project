package com.example.excel_file_processor.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SimpleAppException extends RuntimeException {
    private final HttpStatus status;

    public SimpleAppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
