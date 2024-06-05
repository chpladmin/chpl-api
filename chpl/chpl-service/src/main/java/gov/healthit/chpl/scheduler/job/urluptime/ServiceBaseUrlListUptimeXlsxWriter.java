package gov.healthit.chpl.scheduler.job.urluptime;

import java.awt.Color;
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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic =  "serviceBaseUrlListUptimeCreatorJobLogger")
public class ServiceBaseUrlListUptimeXlsxWriter {
    private static final int WORKSHEET_FONT_POINTS  = 10;
    private static final int WORKSHEET_LARGE_FONT_POINTS = 12;

    private FileUtils fileUtils;
    private String serviceBaseUrlListReportName;
    private CellStyle boldStyle;
    private DateTimeFormatter filepartFormatter;
    private DateTimeFormatter monthHeadingFormatter;
    private DateTimeFormatter lastWeekHeadingFormatter;

    @Autowired
    public ServiceBaseUrlListUptimeXlsxWriter(FileUtils fileUtils,
            @Value("${serviceBaseUrlListReportName}") String serviceBaseUrlListReportName) {
        this.fileUtils = fileUtils;
        this.serviceBaseUrlListReportName = serviceBaseUrlListReportName;
        this.filepartFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        this.monthHeadingFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        this.lastWeekHeadingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
        Sheet sheet = getSheet(workbook, "Service Base URL List Report", null, 0);
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

    private void createDataRow(Workbook workbook, Sheet sheet, int beginRow, ServiceBaseUrlListUptimeReport data) {

    }

    private Sheet getSheet(Workbook workbook, String sheetName, Color tabColor, int lastDataColumn) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            if (sheet instanceof XSSFSheet) {
                XSSFSheet xssfSheet = (XSSFSheet) sheet;
                if (tabColor != null) {
                    DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();
                    XSSFColor xssfTabColor = new XSSFColor(tabColor, colorMap);
                    xssfSheet.setTabColor(xssfTabColor);
                }

                //hide all the columns after the data
                if (lastDataColumn > 0) {
                    CTCol col = xssfSheet.getCTWorksheet().getColsArray(0).addNewCol();
                    col.setMin(lastDataColumn);
                    col.setMax(100); // the last column (1-indexed)
                    col.setHidden(true);
                }
            }
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
            cell.setCellStyle(style);
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
}
