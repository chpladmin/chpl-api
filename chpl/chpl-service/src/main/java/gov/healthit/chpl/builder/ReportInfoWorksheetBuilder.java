package gov.healthit.chpl.builder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

public class ReportInfoWorksheetBuilder {
    private static final int NUM_COLUMNS = 4;

    private Workbook workbook;
    private Font boldFont, smallFont, boldSmallFont, italicSmallFont, boldItalicSmallFont,
        italicUnderlinedSmallFont;
    private CellStyle boldStyle, smallStyle, italicSmallStyle, boldItalicSmallStyle,
        italicUnderlinedSmallStyle, wrappedStyle, sectionNumberingStyle, sectionHeadingStyle,
        tableHeadingStyle;
    private XSSFColor tabColor;
    private PropertyTemplate pt;

    public ReportInfoWorksheetBuilder(final Workbook workbook) {
        this.workbook = workbook;
        pt = new PropertyTemplate();
        initializeFonts();
        initializeStyles();
    }

    /**
     * Creates a formatted Excel worksheet with the information in the report.
     * @param report
     * @return
     */
    public Sheet buildWorksheet(final QuarterlyReportDTO report) throws IOException {
        //create sheet
        Sheet sheet = workbook.createSheet("Report Information");
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();
            tabColor = new XSSFColor(
                    new java.awt.Color(141, 180, 226), colorMap);
            xssfSheet.setTabColor(tabColor);
        }

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        for (int i = 0; i < NUM_COLUMNS; i++) {
            sheet.setDefaultColumnStyle(i, smallStyle);
        }

        createHeader(sheet);
        createSectionOne(sheet, report);
        createSectionTwo(sheet, report);
        createSectionThree(sheet, report);
        createSectionFour(sheet, report);

        //columns B, C, and D need a certain width to get word wrap right
        sheet.setColumnWidth(1, 13171);
        sheet.setColumnWidth(2,  10952);
        sheet.setColumnWidth(3, 10952);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);

        return sheet;
    }

    private void createHeader(final Sheet sheet) {
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellStyle(boldStyle);
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) 2019 "
                + "Report Template for Surveillance Results");
        row = sheet.createRow(2);
        cell = row.createCell(1);
        cell.setCellStyle(boldItalicSmallStyle);
        cell.setCellValue("Template Version: SR19-1.0");
        row = sheet.createRow(4);
        cell = row.createCell(1);
        cell.setCellStyle(italicSmallStyle);
        cell.setCellValue("Instructions");
        row = sheet.createRow(5);
        cell = row.createCell(1);
        cell.setCellStyle(wrappedStyle);
        //increase row height to accommodate four lines of text
        row.setHeightInPoints((4*sheet.getDefaultRowHeightInPoints()));
        //merged cells in rows B,C, and D
        cell.setCellValue("This workbook provides a template for preparing your organization's quarterly "
                + "and annual Surveillance Reports. It is provided for the convenience of ONC-ACBs and is designed "
                + "to be used alongside Program Policy Resources #18-01, #18-02, and #18-03 (October 5, 2018), "
                + "and each ONC-ACB's surveillance plan.\n\n"
                + "Please fill out the boxes below each question and data requested in the included worksheets.");
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 3));
    }

    private void createSectionOne(final Sheet sheet, final QuarterlyReportDTO report) {
        Row row = sheet.createRow(7);
        Cell cell = row.createCell(0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("I.");
        cell = row.createCell(1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Reporting ONC-ACB");
        row = sheet.createRow(8);
        cell = row.createCell(1);
        cell.setCellValue("This report is submitted by the below named ONC-ACB in "
                + "accordance with 45 CFR § 170.523(i)(2) and 45 CFR § 170.556(e).");
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 3));

        row = sheet.createRow(9);
        cell = row.createCell(1);
        cell.setCellValue(report.getAnnualReport().getAcb().getName());
        pt.drawBorders(new CellRangeAddress(9, 9, 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }

    private void createSectionTwo(final Sheet sheet, final QuarterlyReportDTO report) {
        Row row = sheet.createRow(11);
        Cell cell = row.createCell(0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("II.");
        cell = row.createCell(1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Reporting Period");
        row = sheet.createRow(12);
        cell = row.createCell(1);
        cell.setCellValue("This report relates to the following reporting period.");
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 1, 3));

        row = sheet.createRow(13);
        cell = row.createCell(1);
        Calendar quarterStartCal = Calendar.getInstance();
        quarterStartCal.set(Calendar.YEAR, report.getAnnualReport().getYear());
        quarterStartCal.set(Calendar.MONTH, report.getQuarter().getStartMonth()-1);
        quarterStartCal.set(Calendar.DAY_OF_MONTH, report.getQuarter().getStartDay());
        Calendar quarterEndCal = Calendar.getInstance();
        quarterEndCal.set(Calendar.YEAR, report.getAnnualReport().getYear());
        quarterEndCal.set(Calendar.MONTH, report.getQuarter().getEndMonth()-1);
        quarterEndCal.set(Calendar.DAY_OF_MONTH, report.getQuarter().getEndDay());
        DateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy");
        cell.setCellValue(dateFormatter.format(quarterStartCal.getTime()) + " through "
                + dateFormatter.format(quarterEndCal.getTime()));
        pt.drawBorders(new CellRangeAddress(13, 13, 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }

    private void createSectionThree(final Sheet sheet, final QuarterlyReportDTO report) {
        Row row = sheet.createRow(15);
        Cell cell = row.createCell(0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("III.");
        cell = row.createCell(1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Surveillance Activities and Outcomes");
        row = sheet.createRow(16);
        cell = row.createCell(1);
        cell.setCellStyle(wrappedStyle);
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("The ONC-ACB used the following selection method to make its "
                + "random selection of certified Complete EHRs and certified Health IT "
                + "Modules for surveillance initiated during the reporting period.");
        sheet.addMergedRegion(new CellRangeAddress(16, 16, 1, 3));

        row = sheet.createRow(17);
        cell = row.createCell(1);
        cell.setCellStyle(wrappedStyle);
        cell.getCellStyle().setVerticalAlignment(VerticalAlignment.TOP);
        row.setHeightInPoints((3*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("TODO");
        sheet.addMergedRegion(new CellRangeAddress(17, 17, 1, 3));
        pt.drawBorders(new CellRangeAddress(17, 17, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.ALL);

        row = sheet.createRow(19);
        cell = row.createCell(1);
        cell.setCellValue("Please log the surveillance activities and their outcomes to "
                + "the \"Activities and Outcomes\" sheet of this workbook.");
        sheet.addMergedRegion(new CellRangeAddress(19, 19, 1, 3));
    }

    private void createSectionFour(final Sheet sheet, final QuarterlyReportDTO report) {
        Row row = sheet.createRow(21);
        Cell cell = row.createCell(0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("IV.");
        cell = row.createCell(1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Sampling and Selecting");
        row = sheet.createRow(22);
        cell = row.createCell(1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Exclusion and Exhaustion");
        row = sheet.createRow(23);
        cell = row.createCell(1);
        cell.setCellValue("The following certified Complete EHRs and certified "
                + "Health IT Modules were excluded from randomized surveillance for the reasons stated below.");
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 1, 3));

        //this is the beginning of a big table
        row = sheet.createRow(25);
        cell = row.createCell(1);
        cell.setCellStyle(tableHeadingStyle);
        cell.setCellValue("Complete EHR or Health IT Module (CHPL ID)");
        cell = row.createCell(2);
        cell.setCellStyle(tableHeadingStyle);
        cell.setCellValue("Reason(s) for Exclusion");
        int tableStartRow = 25, tableEndRow = 35;
        for (int i = tableStartRow+1; i <= tableEndRow; i++) {
            row = sheet.createRow(i);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            pt.drawBorders(new CellRangeAddress(i, i, 1, 2),
                    BorderStyle.THIN, BorderExtent.TOP);
        }
         //draw border around the table
        pt.drawBorders(new CellRangeAddress(tableStartRow, tableEndRow, 1, 2),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        row = sheet.createRow(37);
        cell = row.createCell(1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Reactive Surveillance");
        row = sheet.createRow(38);
        cell = row.createCell(1);
        cell.setCellStyle(wrappedStyle);
        row.setHeightInPoints((3*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Complete EHR or certified "
                + "Health IT Module. ");
        sheet.addMergedRegion(new CellRangeAddress(38, 38, 1, 3));
    }

    private void initializeFonts() {
        boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short)12);

        smallFont = workbook.createFont();
        smallFont.setFontHeightInPoints((short)10);

        boldSmallFont = workbook.createFont();
        boldSmallFont.setBold(true);
        boldSmallFont.setFontHeightInPoints((short)10);

        italicSmallFont = workbook.createFont();
        italicSmallFont.setItalic(true);
        italicSmallFont.setFontHeightInPoints((short)10);

        boldItalicSmallFont = workbook.createFont();
        boldItalicSmallFont.setBold(true);
        boldItalicSmallFont.setItalic(true);
        boldItalicSmallFont.setFontHeightInPoints((short)10);

        italicUnderlinedSmallFont = workbook.createFont();
        italicUnderlinedSmallFont.setItalic(true);
        italicUnderlinedSmallFont.setUnderline(Font.U_SINGLE);
        italicUnderlinedSmallFont.setFontHeightInPoints((short)10);
    }

    private void initializeStyles() {
        boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        smallStyle = workbook.createCellStyle();
        smallStyle.setFont(smallFont);

        italicSmallStyle = workbook.createCellStyle();
        italicSmallStyle.setFont(italicSmallFont);

        wrappedStyle = workbook.createCellStyle();
        wrappedStyle.setFont(smallFont);
        wrappedStyle.setWrapText(true);

        sectionNumberingStyle = workbook.createCellStyle();
        sectionNumberingStyle.setAlignment(HorizontalAlignment.RIGHT);
        sectionNumberingStyle.setFont(boldSmallFont);

        sectionHeadingStyle = workbook.createCellStyle();
        sectionHeadingStyle.setAlignment(HorizontalAlignment.LEFT);
        sectionHeadingStyle.setFont(boldSmallFont);

        boldItalicSmallStyle = workbook.createCellStyle();
        boldItalicSmallStyle.setFont(boldItalicSmallFont);

        italicUnderlinedSmallStyle = workbook.createCellStyle();
        italicUnderlinedSmallStyle.setFont(italicUnderlinedSmallFont);

        tableHeadingStyle = workbook.createCellStyle();
        tableHeadingStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        tableHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
}
