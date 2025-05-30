package com.example.excel_file_processor.service;


import com.example.excel_file_processor.model.GradingResponse;
import com.example.excel_file_processor.model.GradingResult;
import com.example.excel_file_processor.model.GradingRow;
import com.example.excel_file_processor.model.SimpleAppException;
import com.example.excel_file_processor.util.GradingHandler.GradingHandler;
import com.example.excel_file_processor.util.GradingHandler.GradingHandlerFactory;
import com.example.excel_file_processor.util.WorkbookParser;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GradingService {

    private final WorkbookParser workbookParser;
    private final GradingHandlerFactory gradingHandlerFactory;

    public GradingResponse gradeWorkbook(MultipartFile masterFile, MultipartFile studentFile) {
        Workbook masterWorkbook = parseWorkbook(masterFile);
        Workbook studentWorkbook = parseWorkbook(studentFile);

        Sheet gradingSheet = extractGradingSheet(masterWorkbook);
        List<GradingRow> gradingInstructions = extractGradingRows(gradingSheet);

        GradingResult results = applyGrading(gradingInstructions, masterWorkbook, studentWorkbook);

        return buildGradingResponse(results);
    }

    private Workbook parseWorkbook(MultipartFile file) {
        try {
            return workbookParser.loadWorkBook(file);
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

    private List<GradingRow> extractGradingRows(Sheet gradingSheet) {
        List<GradingRow> instructions = new ArrayList<>();
        Iterator<Row> iterator = gradingSheet.rowIterator();

        if (iterator.hasNext()) iterator.next(); // skip header row

        while (iterator.hasNext()) {
            Row row = iterator.next();
            GradingRow gradeRow = GradingRow.fromRow(row, workbookParser);

            //Grade row is null when all grading columns are empty or blank
            if (gradeRow != null) {
                instructions.add(gradeRow);
            }
        }

        return instructions;
    }

    private GradingResult applyGrading(List<GradingRow> instructions,
                                       Workbook master,
                                       Workbook student) {
        GradingResult result = new GradingResult();
        result.setMaxScore(instructions.size());

        for (GradingRow instruction : instructions) {
            if (instruction.hasError()) {
                result.addError(instruction.getErrorMessage());
                continue;
            }

            try {
                CellRangeAddress range = workbookParser.getRangeAddressFromRangeString(instruction.getGradingRange());

                Sheet targetSheet = requireSheet(master.getSheet(instruction.getSheetName()), instruction.getSheetName(), "master", result);
                if (targetSheet == null) continue;

                Sheet studentSheet = requireSheet(student.getSheet(instruction.getSheetName()), instruction.getSheetName(), "student", result);
                if (studentSheet == null) continue;

                CellWalk cellWalk = new CellWalk(targetSheet, range);
                cellWalk.setTraverseEmptyCells(true);
                GradingHandler grader = gradingHandlerFactory.createHandler(instruction.getComparisonType(), studentSheet, workbookParser);
                cellWalk.traverse(grader);

                if (grader.isPassing()) {
                    result.incrementTotalScore();
                }
            } catch (IllegalArgumentException e) {
                result.addError("Error grading value: No cells found in master sheet: " + instruction.getSheetName() + " cell: " + instruction.getGradingRange());
            }
        }

        return result;
    }

    private Sheet requireSheet(Sheet sheet, String sheetName, String role, GradingResult result) {
        if (sheet == null) {
            result.getErrors().add("Error grading value: No sheet found in " + role + " sheet: " + sheetName);
        }
        return sheet;
    }

    private GradingResponse buildGradingResponse(GradingResult results) {
        LocalDateTime now = LocalDateTime.now();
        double percentage = BigDecimal.valueOf((double) results.getTotalScore() / results.getMaxScore() * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return new GradingResponse(results.getTotalScore(), results.getMaxScore(), results.getErrors(), now.toString(), now.toString(), percentage);
    }
}
