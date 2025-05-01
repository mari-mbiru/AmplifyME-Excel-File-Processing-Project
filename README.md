# Excel Grading API - Spring Boot Project

## Overview

This Spring Boot application provides an API for comparing Excel files according to defined grading rules. It uses
Apache POI to parse and evaluate cell values and formulas, returning a score and error breakdown.

## Features

- Accepts master and student Excel files
- Parses grading rules from the "Grading" sheet in master file
- Supports CorrVal (value-based) and CorrForm (formula-based) comparisons
- Returns detailed scoring and error information
- Bonus: Dockerized for easy deployment

## Getting Started

### Prerequisites

- Java 21+
- Maven or Gradle
- Docker (optional, for containerization)
- Postman or curl for testing

### Running the Application

#### Using Bash

```bash
./graldew spring-boot:run
```

OR

```bash
./mvnw spring-boot:run
```

#### Using Docker

```bash
docker build -t excel-grader .
docker run -p 8080:8080 excel-grader
```

### API Documentation

#### 1. Endpoint

> POST http://localhost:8080/api/v1/excel/grade

#### 2. Required Headers

| Header       | Value               |
|--------------|---------------------|
| Content-Type | multipart/form-data |

#### 3. Request Parameters

| Parameter Name | 	Required | 	Validations                                       |
|----------------|-----------|----------------------------------------------------|
| master_file    | Yes       | Must be a valid Excel (2007+ version) file (.xlsx) |
| student_file   | Yes       | Must be a valid Excel (2007+ version) file (.xlsx) |

#### 5. Response Example

```
{
  "totalScore": 8,
  "maxScore": 10,
  "errors": ["Error grading value: ..."],
  "createdAt": "2025-04-24T14:50:30.111779",
  "updatedAt": "2025-04-24T14:50:30.111780",
  "percentage": 80
}

``` 

### Assumptions

- If the cell range is invalid, it is considered an error.
- If the string representation of the cells to be compared in the master and student sheet are equal then the grade
  passes (Formulas will not be evaluated).
- The Master workbook will always contain a worksheet called "Grading". The "Grading" worksheet will have the first
  three columns containing the Worksheet Name, Cell Range and Comparison Type for grading. The first row will always be
  a header row describing these columns. If the Grading sheet is missing no grading should proceed.
- A row in the grading sheet where the first three columns are empty will be assumed to be an empty row that should not
  affect max score.
- A Grading row with 1 or 2 missing/invalid values will affect max score, but return a grading error.
- The files submitted will use the new .xlsx (XML Spreadsheet Format) format as it has been in use since 2007.
- The master and student files will be relatively small (less than ten thousand rows).
- Grading types "CorrForm" and "CorrVal" are only valid as spelled. Lowercase values equivalents of this are allowed.

### Design Decisions

- Apache POI is used for maximum control over Excel file parsing.
- The Apache POI CellWalk Api is used for traversing the cell ranges defined in the grading rows. This enables efficient
  traversal of multidimensional cell ranges.
- Grading is done by implementing the CellHandler interface in an abstract Grading class. This enables the use of
  different grading strategies in case new Grading types are added. This design decision when I thought the CorrForm and
  CorrVal would require separate handlers.
- Cell value comparison for both CorrVal and CorrForm approaches is done by converting all cell values to a string
  regardless of type.
  - NUMERIC type: significant digits are kept for numbers and fractions (so 1/3 and 0.33333 will have different string
    value) while dates are converted to strings directly.
  - BLANK and _NONE types: treated as an empty string.
  - ERROR type: returns the string name of the type or error
  - STRING and FORMULA types: return the string or string representation of the formula.
- Error handling is centralized using @ControllerAdvice to return error messages that are clearer to read and determine
  the cause of.
- Comparison logic is separated from controller logic for testability.

### Testing

Unit tests are implemented using JUnit and cover:

- Workbook parsing
- Cell value comparison
- Formula extraction
- Grading logic

Integration tests cover:

- Full API endpoint
- Success and error scenarios
- Grading of all files provided in the project briefing.
  > In the file : "Candidate Sample Answer Sheet CorrVal + CorrForm" the cell C8 in the Grading sheet had the value "
  CorForm", this was considered an error and modified to "CorrForm" ensure tests passed to match the expected value in
  the project brief
- Created some additional excel workbook files to be used for integration testing. 