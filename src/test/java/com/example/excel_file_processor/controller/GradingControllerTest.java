package com.example.excel_file_processor.controller;

import com.example.excel_file_processor.model.GradingResponse;
import com.example.excel_file_processor.service.GradingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = GradingController.class)
public class GradingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GradingService gradingService;

    @Test
    void gradeExcelSheet_shouldReturnGradingResponse_whenFilesAreValid() throws Exception {
        //Given
        String dateString = LocalDateTime.now().toString();
        GradingResponse mockResponse = new GradingResponse(9.0, 10.0, List.of("Error Grading"), dateString, dateString, 90.0);

        // When
        Mockito.when(gradingService.gradeWorkbook(Mockito.any(), Mockito.any()))
                .thenReturn(mockResponse);

        MockMultipartFile masterFile = new MockMultipartFile("master_file", "master.xlsx", "application/vnd.ms-excel", new byte[10]);
        MockMultipartFile studentFile = new MockMultipartFile("student_file", "student.xlsx", "application/vnd.ms-excel", new byte[10]);

        ResultActions result = mockMvc.perform(multipart("/api/v1/excel/grade")
                .file(masterFile)
                .file(studentFile));

        //Then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").value(9.0))
                .andExpect(jsonPath("$.maxScore").value(10.0))
                .andExpect(jsonPath("$.totalScore").value(9.0))
                .andExpect(jsonPath("$.errors", contains("Error Grading")))
                .andExpect(jsonPath("$.updatedAt").value(dateString))
                .andExpect(jsonPath("$.createdAt").value(dateString))
                .andExpect(jsonPath("$.percentage").value(90.0));
    }


    @Test
    void gradeExcelSheet_shouldReturnBadRequest_whenAnyFileIsNotXLSX() throws Exception {

        //Given
        MockMultipartFile invalidFile = new MockMultipartFile("master_file", "file.txt", "text/plain", "dummy".getBytes());
        MockMultipartFile studentFile = new MockMultipartFile("student_file", "student.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "dummy".getBytes());

        //When
        ResultActions result = mockMvc.perform(multipart("/api/v1/excel/grade")
                .file(invalidFile)
                .file(studentFile));

        //Then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only .xlsx files are supported for grading."));
    }

    @Test
    void gradeExcelSheet_shouldReturnBadRequest_whenAnyFileIsNotIncluded() throws Exception {

        //Given
        MockMultipartFile studentFile = new MockMultipartFile("student_file", "student.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "dummy".getBytes());

        //When
        ResultActions result = mockMvc.perform(multipart("/api/v1/excel/grade")
                .file(studentFile));

        //Then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'master_file' is required"));
    }
}
