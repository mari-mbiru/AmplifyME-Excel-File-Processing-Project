package com.example.excel_file_processor.service;


import com.example.excel_file_processor.model.GradingResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GradingService {

    public GradingResponse gradeMasterAndStudentFiles(MultipartFile masterFile, MultipartFile studentFile) {
        return new GradingResponse(0.0, 0.0, null, null, null, 100.0);
    }
}
