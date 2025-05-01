package com.example.excel_file_processor.service;

import com.example.excel_file_processor.model.GradingResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class GradingServiceIntegrationTest {

    @Autowired
    GradingService gradingService;

    @CsvSource({
            "'Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx','Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx',10,10,0",
            "'Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct).xlsx','Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct).xlsx',10,8,2",
            "'Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx','Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx',10,3,0",
            "'Candidate Sample Answer Sheet CorrVal + CorrForm.xlsx','Candidate Sample Answer Sheet CorrVal + CorrForm.xlsx',10,10,0",
            "'Candidate Sample Answer Sheet Break In Grading Rows(10 correct).xlsx','Candidate Sample Answer Sheet Break In Grading Rows(10 correct).xlsx',10,10,0",
            "'Candidate Sample Answer Sheet Invalid Grading Instructions(4 correct).xlsx','Candidate Sample Answer Sheet Invalid Grading Instructions(4 correct).xlsx',10,4,6",
            "'Candidate Sample Master Sheet Value Types.xlsx','Candidate Sample Student Sheet Value Types (10 correct).xlsx',18,10,0"
    })
    @ParameterizedTest
    void gradeVariousStudentFiles(String masterFileName, String studentFileName, int max, int total, int errorCount) throws Exception {

        //Given
        MultipartFile masterFile = getTestFile("fixtures/" + masterFileName);
        MultipartFile studentFile = getTestFile("fixtures/" + studentFileName);

        //When
        GradingResponse response = gradingService.gradeWorkbook(masterFile, studentFile);

        //Then
        assertEquals(max, response.maxScore());
        assertEquals(total, response.totalScore());
        assertEquals(errorCount, response.errors().size());
    }

    private MultipartFile getTestFile(String path) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(inputStream, "Could not find test file: " + path);
        return new MockMultipartFile("file", path, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", inputStream);
    }
}
