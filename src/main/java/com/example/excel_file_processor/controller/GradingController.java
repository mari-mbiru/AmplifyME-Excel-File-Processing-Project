package com.example.excel_file_processor.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/excel")
public class GradingController {

    @PostMapping(path = "/grade", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    ResponseEntity<String> gradeExcelSheet( @RequestParam("master_file") MultipartFile masterFile,
                                            @RequestParam("student_file") MultipartFile studentFile){
        return ResponseEntity.ok(masterFile.getName());
    }
}
