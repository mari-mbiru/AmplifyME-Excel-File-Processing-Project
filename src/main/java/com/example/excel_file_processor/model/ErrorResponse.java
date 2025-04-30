package com.example.excel_file_processor.model;

public record ErrorResponse(int status, String error, String message, String path) {
}