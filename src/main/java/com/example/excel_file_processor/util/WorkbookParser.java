package com.example.excel_file_processor.util;

import org.apache.poi.EmptyFileException;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WorkbookParser {

    private static final Pattern validRangePattern = Pattern.compile("^(?<start>[A-Z]{1,3}[0-9]{1,7})(?::(?<end>[A-Z]{1,3}[0-9]{1,7}))?$");

    public Workbook loadWorkBook(MultipartFile file) throws IOException {
        String filename = Optional.of(file.getOriginalFilename()).orElse("").toLowerCase();

        if (!filename.endsWith(".xlsx")) {
            throw new IllegalArgumentException("Unsupported file format. Only .xlsx files are supported");
        }

        try (InputStream is = file.getInputStream()) {
            return new XSSFWorkbook(is);
        } catch (POIXMLException | OLE2NotOfficeXmlFileException | EmptyFileException e) {
            throw new IllegalArgumentException("The uploaded file is not a valid .xlsx Excel file", e);
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

        String startCellAddress = matcher.group("start");
        String endCellAddress = Optional.ofNullable(matcher.group("end")).orElse(startCellAddress);

        CellReference start = new CellReference(startCellAddress);
        CellReference end = new CellReference(endCellAddress);

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
