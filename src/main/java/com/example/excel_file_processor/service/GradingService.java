package com.example.excel_file_processor.service;


import com.example.excel_file_processor.model.GradeRow;
import com.example.excel_file_processor.model.GradingResponse;
import com.example.excel_file_processor.model.GradingResult;
import com.example.excel_file_processor.model.SimpleAppException;
import com.example.excel_file_processor.util.ExcelWorkbookParser;
import com.example.excel_file_processor.util.GradingHandler.AbstractGradingHandler;
import com.example.excel_file_processor.util.GradingHandler.GradingHandlerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.cellwalk.CellWalk;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GradingService {

    private final ExcelWorkbookParser workbookParser;
    private final GradingHandlerFactory gradingHandlerFactory;
    public GradingResponse gradeMasterAndStudentFiles(MultipartFile masterFile, MultipartFile studentFile) {
        Workbook masterWorkbook = parseWorkbook(masterFile);
        Workbook studentWorkbook = parseWorkbook(studentFile);

        Sheet gradingSheet = extractGradingSheet(masterWorkbook);
        List<GradeRow> gradingInstructions = extractGradingInstructions(gradingSheet);

        GradingResult results = applyGradingInstructions(gradingInstructions, masterWorkbook, studentWorkbook);

        return buildGradingResponse(results);
    }

    private Workbook parseWorkbook(MultipartFile file) {
        try {
            return workbookParser.getWorkbookFromFile(file);
        } catch (IOException e) {
            throw new SimpleAppException("There was a problem parsing one of the workbooks.", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (IllegalArgumentException e) {
            throw new SimpleAppException("Unsupported file format. Only .xlsx files are supported", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Sheet extractGradingSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Grading");
        if (sheet == null) {
            throw new SimpleAppException("The masterfile is missing the Grading sheet.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return sheet;
    }

    private List<GradeRow> extractGradingInstructions(Sheet gradingSheet) {
        List<GradeRow> instructions = new ArrayList<>();
        Iterator<Row> iterator = gradingSheet.rowIterator();

        if (iterator.hasNext()) iterator.next(); // skip header row

        while (iterator.hasNext()) {
            Row row = iterator.next();
            GradeRow gradeRow = GradeRow.fromRow(row, workbookParser);
            if (gradeRow != null) {
                instructions.add(GradeRow.fromRow(row, workbookParser));
            }
        }

        return instructions;
    }

    private GradingResult applyGradingInstructions(List<GradeRow> instructions,
                                                   Workbook master,
                                                   Workbook student) {
        GradingResult result = new GradingResult();

        for (GradeRow instruction : instructions) {
            if (instruction.hasError()) {
                result.getErrors().add(instruction.getErrorMessage());
                result.setMaxScore(result.getMaxScore() + 1);
                continue;
            }

            try {
                CellRangeAddress range = workbookParser.getRangeAddressFromRangeString(instruction.getGradingRange());

                Sheet targetSheet = master.getSheet(instruction.getSheetName());
                Sheet studentSheet = student.getSheet(instruction.getSheetName());

                if (targetSheet == null) {
                    result.getErrors().add("Error grading value: No sheet found in master sheet: " + instruction.getSheetName());
                    result.setMaxScore(result.getMaxScore() + 1);
                    continue;
                }

                if (studentSheet == null) {
                    result.getErrors().add("Error grading value: No sheet found in student sheet: " + instruction.getSheetName());
                    result.setMaxScore(result.getMaxScore() + 1);
                    continue;
                }

                CellWalk cellWalk = new CellWalk(targetSheet, range);
                cellWalk.setTraverseEmptyCells(true);
                AbstractGradingHandler grader = gradingHandlerFactory.createHandler(instruction.getComparisonType(), studentSheet, workbookParser);
                cellWalk.traverse(grader);

                if (grader.isPassing()) {
                    result.setTotalScore(result.getTotalScore() + 1);
                    result.setMaxScore(result.getMaxScore() + 1);
                } else {
                    result.getErrors().add("Value mismatch");
                }
            } catch (IllegalArgumentException e) {
                result.getErrors().add("Error grading value: No cells found in master sheet: " + instruction.getSheetName() + " cell: " + instruction.getGradingRange());
            } finally {
                result.setMaxScore(result.getMaxScore() + 1);
            }
        }

        return result;
    }

    private GradingResponse buildGradingResponse(GradingResult results) {

        double percentage = (double) results.getTotalScore() / results.getMaxScore();
        return new GradingResponse(results.getTotalScore(), results.getMaxScore(), results.getErrors(), LocalDateTime.now().toString(), LocalDateTime.now().toString(), percentage);
    }

}
