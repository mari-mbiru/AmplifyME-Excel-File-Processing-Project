package com.example.excel_file_processor.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExcelWorkbookParser {

    private static final Pattern validRangePattern = Pattern.compile("^(?<start>[A-Z]{1,3}[0-9]{1,7})(?::(?<end>[A-Z]{1,3}[0-9]{1,7}))?$");

    public Workbook getWorkbookFromFile(MultipartFile file) throws IOException {

        if (file.getOriginalFilename().endsWith(".xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only .xlsx files are supported");
        }
    }

    public String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return formatNumericValue(cell.getNumericCellValue());
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            //Includes the case cell type is BLANK.
            default:
                return "";
        }
    }

    private String formatNumericValue(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value); // e.g., 100.0 → "100"
        } else {
            return BigDecimal.valueOf(value)
                    .stripTrailingZeros()
                    .toPlainString(); // e.g., 100.25 → "100.25"
        }
    }


    public CellRangeAddress getRangeAddressFromRangeString(String rangeString) {
        if (rangeString == null || rangeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Cell range string is empty or null.");
        }

        Matcher matcher = validRangePattern.matcher(rangeString.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid cell range format: " + rangeString);
        }

        String startCell = matcher.group("start");
        String endCell = Optional.ofNullable(matcher.group("end")).orElse(startCell);

        CellReference start = new CellReference(startCell);
        CellReference end = new CellReference(endCell);

        if (start.getRow() > end.getRow() || start.getCol() > end.getCol()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid cell range: '%s'. Range must go from top-left to bottom-right.", rangeString));
        }

        return new CellRangeAddress(
                start.getRow(), end.getRow(),
                start.getCol(), end.getCol()
        );
    }


}
