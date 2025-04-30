package com.example.excel_file_processor.controller;


import com.example.excel_file_processor.model.GradingResponse;
import com.example.excel_file_processor.model.SimpleAppException;
import com.example.excel_file_processor.service.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    @PostMapping(path = "/grade", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    ResponseEntity<GradingResponse> gradeExcelSheet(@RequestParam("master_file") MultipartFile masterFile,
                                                    @RequestParam("student_file") MultipartFile studentFile) {

        if (!masterFile.getOriginalFilename().endsWith(".xlsx") || !studentFile.getOriginalFilename().endsWith(".xlsx")) {
            throw new SimpleAppException("Only .xlsx files are supported for grading.", HttpStatus.BAD_REQUEST);
        }

        GradingResponse result = gradingService.gradeMasterAndStudentFiles(masterFile, studentFile);
        return ResponseEntity.ok(result);
    }
}
