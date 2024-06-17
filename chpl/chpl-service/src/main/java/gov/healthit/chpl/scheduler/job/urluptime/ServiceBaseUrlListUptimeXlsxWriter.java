package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic =  "serviceBaseUrlListUptimeCreatorJobLogger")
public class ServiceBaseUrlListUptimeXlsxWriter {
    private static final int DAYS_IN_WEEK = 7;
    private static final int WORKSHEET_LARGE_FONT_POINTS = 12;

    private FileUtils fileUtils;
    private String serviceBaseUrlListReportName;
    private CellStyle boldStyle, percentageStyle;
    private DateTimeFormatter filepartFormatter;
    private DateTimeFormatter monthHeadingFormatter;
    private DateTimeFormatter lastWeekHeadingFormatter;
    private String unformattedDeveloperUrl;

    @Autowired
    public ServiceBaseUrlListUptimeXlsxWriter(FileUtils fileUtils,
            @Value("${serviceBaseUrlListReportName}") String serviceBaseUrlListReportName,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.fileUtils = fileUtils;
        this.serviceBaseUrlListReportName = serviceBaseUrlListReportName;
        this.filepartFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        this.monthHeadingFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        this.lastWeekHeadingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.unformattedDeveloperUrl = chplUrlBegin + developerUrlPart;
    }

    public void writeWorkbookAsFile(List<ServiceBaseUrlListUptimeReport> reportRows) {
        Workbook workbook = createWorkbook(reportRows);

        //write out the workbook contents to this file
        OutputStream outputStream = null;
        try {
            File fileToWrite = createFile();
            outputStream = new FileOutputStream(fileToWrite);
            LOGGER.info("Writing service base url report xlsx file to " + fileToWrite.getAbsolutePath());
            workbook.write(outputStream);
        } catch (Exception ex) {
            LOGGER.error("Error writing service base url report workbook to file", ex);
        } finally {
            try {
                outputStream.flush();
            } catch (Exception ignore) {
            }
            try {
                outputStream.close();
            } catch (Exception ignore) {
            }
        }
    }

    private File createFile() {
        File workbookFile = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            workbookFile = fileUtils.createDownloadFile(serviceBaseUrlListReportName + "-" + now.format(filepartFormatter) + " .xlsx");
        } catch (IOException ex) {
            LOGGER.error("There was an error creating the publicly downloadable Service Base URL List file.", ex);
        }
        return workbookFile;
    }

    private Workbook createWorkbook(List<ServiceBaseUrlListUptimeReport> reportRows) {
        XSSFWorkbookFactory workbookFactory = new XSSFWorkbookFactory();
        Workbook workbook = workbookFactory.create();
        Sheet dataSheet = getSheet(workbook, "Service Base URL List Report");
        AtomicInteger currDataRow = new AtomicInteger(0);
        createHeader(workbook, dataSheet, currDataRow.getAndIncrement());
        reportRows.stream()
            .forEach(row -> createDataRow(workbook, dataSheet, currDataRow.getAndIncrement(), row));

        buildDefinitionSheet(workbook);
        return workbook;
    }

    private void createHeader(Workbook workbook, Sheet sheet, int rowNum) {
        Row row = getRow(sheet, rowNum);
        AtomicInteger currCol = new AtomicInteger(0);
        List<String> headers = getHeaders();
        headers.stream()
            .forEach(header -> {
                Cell cell = createCell(row, currCol.getAndIncrement(), getBoldStyle(workbook));
                cell.setCellValue(header);
            });
    }

    private List<String> getHeaders() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate aWeekAgoBegin = LocalDate.now().minusDays(DAYS_IN_WEEK);

        return Arrays.asList(
                "Developer",
                "Developer URL",
                "Service Base URL List",
                "All Time Total Tests",
                "All Time Successful Tests",
                "All Time Successful Tests Percentage",
                String.format("%s Total Tests", monthHeadingFormatter.format(yesterday)),
                String.format("%s Successful Tests", monthHeadingFormatter.format(yesterday)),
                String.format("%s Successful Tests Percentage", monthHeadingFormatter.format(yesterday)),
                String.format("%s - %s Total Tests", lastWeekHeadingFormatter.format(aWeekAgoBegin), lastWeekHeadingFormatter.format(yesterday)),
                String.format("%s - %s Successful Tests", lastWeekHeadingFormatter.format(aWeekAgoBegin), lastWeekHeadingFormatter.format(yesterday)),
                String.format("%s - %s Successful Tests Percentage", lastWeekHeadingFormatter.format(aWeekAgoBegin), lastWeekHeadingFormatter.format(yesterday)));
    }

    private void createDataRow(Workbook workbook, Sheet sheet, int rowNum, ServiceBaseUrlListUptimeReport data) {
        Row row = getRow(sheet, rowNum);
        AtomicInteger currCol = new AtomicInteger(0);

        Cell cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getDeveloperName());

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(String.format(unformattedDeveloperUrl, data.getDeveloperId().toString()));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getUrl());

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getTotalTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getTotalSuccessfulTestCount());

        Cell totalSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        totalSuccessfulPercentageCell.setCellFormula("E" + (rowNum + 1) + "/D" + (rowNum + 1));
        CreationHelper creationHelper = workbook.getCreationHelper();
        FormulaEvaluator formulaEvaluator = creationHelper.createFormulaEvaluator();
        formulaEvaluator.evaluateFormulaCell(totalSuccessfulPercentageCell);
        totalSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getCurrentMonthTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getCurrentMonthSuccessfulTestCount());

        Cell monthSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        monthSuccessfulPercentageCell.setCellFormula("H" + (rowNum + 1) + "/G" + (rowNum + 1));
        formulaEvaluator.evaluateFormulaCell(monthSuccessfulPercentageCell);
        monthSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getPastWeekTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getPastWeekSuccessfulTestCount());

        Cell weekSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        weekSuccessfulPercentageCell.setCellFormula("K" + (rowNum + 1) + "/J" + (rowNum + 1));
        formulaEvaluator.evaluateFormulaCell(weekSuccessfulPercentageCell);
        weekSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));
    }

    private void buildDefinitionSheet(Workbook workbook) {
        Sheet definitionSheet = getSheet(workbook, "Definitions");
        AtomicInteger currRow = new AtomicInteger(0);
        Row row = getRow(definitionSheet, currRow.getAndIncrement());
        AtomicInteger currCol = new AtomicInteger(0);

        //heading
        Cell cell = createCell(row, currCol.getAndIncrement(), getBoldStyle(workbook));
        cell.setCellValue("Column Name");
        cell = createCell(row, currCol.getAndIncrement(), getBoldStyle(workbook));
        cell.setCellValue("Description");

        //content
        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("Developer");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The name of the developer or vendor of the certified health IT product");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("Developer URL");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("Link to the developer’s page in CHPL");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("Service Base URL List");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The URL of the Service Base URL List");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("All Time Total Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of tests conducted since monitoring began");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("All Time Successful Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of successful tests since monitoring began");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("All Time Successful Tests, Percentage");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The percentage of successful tests since monitoring began");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Previous Month] Total Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of tests conducted in the previous month");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Previous Month] Successful Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of successful tests in the previous month");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Previous Month] Successful Tests, Percentage");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The percentage of successful tests in the previous month");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Last 7-Days] Total Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of tests conducted in the last 7 days");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Last 7-Days] Successful Tests");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The total number of successful tests in the last 7 days");

        row = getRow(definitionSheet, currRow.getAndIncrement());
        currCol.set(0);
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("[Last 7-Days] Successful Tests, Percentage");
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue("The percentage of successful tests in the last 7 days");
    }

    private Sheet getSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    private Row getRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    public Cell createCell(Row row, int cellIndex, CellStyle style) {
        Cell cell = null;
        try {
            cell = row.createCell(cellIndex);
            if (style != null) {
                cell.setCellStyle(style);
            }
        } catch (Exception ex) {
            LOGGER.error("Error creating cell in row " + row.getRowNum() + " at column " + cellIndex, ex);
        }
        return cell;
    }

    public CellStyle getBoldStyle(Workbook workbook) {
        if (this.boldStyle != null) {
            return this.boldStyle;
        }
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) WORKSHEET_LARGE_FONT_POINTS);

        this.boldStyle = workbook.createCellStyle();
        this.boldStyle.setFont(boldFont);
        this.boldStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        this.boldStyle.setFillBackgroundColor(IndexedColors.WHITE.index);
        return this.boldStyle;
    }

    public CellStyle getPercentageStyle(Workbook workbook) {
        if (this.percentageStyle != null) {
            return this.percentageStyle;
        }
        this.percentageStyle = workbook.createCellStyle();
        this.percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        return this.percentageStyle;
    }
}
