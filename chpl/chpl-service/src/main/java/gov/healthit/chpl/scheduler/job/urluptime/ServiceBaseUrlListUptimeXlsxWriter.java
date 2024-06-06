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

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic =  "serviceBaseUrlListUptimeCreatorJobLogger")
public class ServiceBaseUrlListUptimeXlsxWriter {
    private static final int WORKSHEET_LARGE_FONT_POINTS = 12;

    private FileUtils fileUtils;
    private String serviceBaseUrlListReportName;
    private CellStyle boldStyle, linkStyle, percentageStyle;
    private DateTimeFormatter filepartFormatter;
    private DateTimeFormatter monthHeadingFormatter;
    private DateTimeFormatter lastWeekHeadingFormatter;
    private String unformtatedDeveloperUrl;

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
        this.unformtatedDeveloperUrl = chplUrlBegin + developerUrlPart;
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
        Sheet sheet = getSheet(workbook, "Service Base URL List Report");
        AtomicInteger currRow = new AtomicInteger(0);
        createHeader(workbook, sheet, currRow.getAndIncrement());
        reportRows.stream()
            .forEach(row -> createDataRow(workbook, sheet, currRow.getAndIncrement(), row));
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
        LocalDate aWeekAgoBegin = LocalDate.now().minusDays(8);

        return Arrays.asList(
                "Developer",
                "URL",
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
        CreationHelper creationHelper = workbook.getCreationHelper();
        XSSFHyperlink link = (XSSFHyperlink) creationHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(String.format(unformtatedDeveloperUrl, data.getDeveloperId().toString()));
        cell.setHyperlink((XSSFHyperlink) link);
        cell.setCellStyle(getLinkStyle(workbook));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getUrl());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getTotalTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getTotalSuccessfulTestCount());

        Cell totalSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        totalSuccessfulPercentageCell.setCellFormula("D" + (rowNum + 1) + "/C" + (rowNum + 1));
        FormulaEvaluator formulaEvaluator = creationHelper.createFormulaEvaluator();
        formulaEvaluator.evaluateFormulaCell(totalSuccessfulPercentageCell);
        totalSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getCurrentMonthTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getCurrentMonthSuccessfulTestCount());

        Cell monthSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        monthSuccessfulPercentageCell.setCellFormula("G" + (rowNum + 1) + "/F" + (rowNum + 1));
        formulaEvaluator.evaluateFormulaCell(monthSuccessfulPercentageCell);
        monthSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));

        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getPastWeekTestCount());
        cell = createCell(row, currCol.getAndIncrement(), null);
        cell.setCellValue(data.getPastWeekSuccessfulTestCount());

        Cell weekSuccessfulPercentageCell = createCell(row, currCol.getAndIncrement(), null);
        weekSuccessfulPercentageCell.setCellFormula("J" + (rowNum + 1) + "/I" + (rowNum + 1));
        formulaEvaluator.evaluateFormulaCell(weekSuccessfulPercentageCell);
        weekSuccessfulPercentageCell.setCellStyle(getPercentageStyle(workbook));
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

    public CellStyle getLinkStyle(Workbook workbook) {
        if (this.linkStyle != null) {
            return this.linkStyle;
        }
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.index);

        this.linkStyle = workbook.createCellStyle();
        this.linkStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        this.linkStyle.setFillBackgroundColor(IndexedColors.WHITE.index);
        this.linkStyle.setFont(linkFont);
        return this.linkStyle;
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
