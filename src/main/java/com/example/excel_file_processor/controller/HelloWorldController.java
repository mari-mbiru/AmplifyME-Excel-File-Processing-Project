package com.example.excel_file_processor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HelloWorldController {

    @GetMapping("/say-hello")
    ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello World");
    }
}
