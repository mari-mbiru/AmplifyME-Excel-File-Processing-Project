package com.example.excel_file_processor.exception;

import com.example.excel_file_processor.model.ErrorResponse;
import com.example.excel_file_processor.model.SimpleAppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(SimpleAppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(SimpleAppException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestPartException ex, HttpServletRequest request) {
        String name = ex.getRequestPartName();
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Parameter '" + name + "' is required", request.getRequestURI()),
                HttpStatus.BAD_REQUEST
        );
    }
}
