package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

public class ReportInfoWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 6;
    private static final int LAST_DATA_ROW = 60;
    private PropertyTemplate pt;

    public ReportInfoWorksheetBuilder(final Workbook workbook) {
        super(workbook);
        pt = new PropertyTemplate();
    }

    @Override
    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    @Override
    public int getLastDataRow() {
        return LAST_DATA_ROW;
    }

    /**
     * Creates a formatted Excel worksheet with the information in the reports.
     * @param report
     * @return
     */
    public Sheet buildWorksheet(final List<QuarterlyReportDTO> reports) throws IOException {
        //create sheet or get the sheet if it already exists
        Sheet sheet = getSheet("Report Information", new Color(141, 180, 226));

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        createHeader(sheet);
        createSectionOne(sheet, reports);
        createSectionTwo(sheet, reports);
        createSectionThree(sheet, reports);
        createSectionFour(sheet, reports);
        createSectionFive(sheet, reports);
        createSectionSix(sheet);

        //columns B, C, and D need a certain width to get word wrap right
        sheet.setColumnWidth(1, getColumnWidth(51));
        sheet.setColumnWidth(2,  getColumnWidth(42));
        sheet.setColumnWidth(3, getColumnWidth(42));

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);

        return sheet;
    }

    private void createHeader(final Sheet sheet) {
        Row row = sheet.createRow(1);
        Cell cell = createCell(row, 1);
        cell.setCellStyle(boldStyle);
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) 2019 "
                + "Report Template for Surveillance Results");
        row = sheet.createRow(2);
        cell = createCell(row, 1);
        cell.setCellStyle(boldItalicSmallStyle);
        cell.setCellValue("Template Version: SR19-1.0");
        row = sheet.createRow(4);
        cell = createCell(row, 1);
        cell.setCellStyle(italicSmallStyle);
        cell.setCellValue("Instructions");
        row = sheet.createRow(5);
        cell = createCell(row, 1);
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

    private void createSectionOne(final Sheet sheet, final List<QuarterlyReportDTO> reports) {
        Row row = sheet.createRow(7);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("I.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Reporting ONC-ACB");
        row = sheet.createRow(8);
        cell = createCell(row, 1);
        cell.setCellValue("This report is submitted by the below named ONC-ACB in "
                + "accordance with 45 CFR § 170.523(i)(2) and 45 CFR § 170.556(e).");
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 3));

        row = sheet.createRow(9);
        cell = createCell(row, 1);
        //make sure we start with a unique set of ACB names
        //and build the report's ACB name from that (in reality i think all acb names will be the same)
        Set<String> acbNames = new HashSet<String>();
        for (QuarterlyReportDTO report : reports) {
            acbNames.add(report.getAcb().getName());
        }
        StringBuffer buf = new StringBuffer();
        for (String acbName : acbNames) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append(acbName);
        }
        cell.setCellValue(buf.toString());
        pt.drawBorders(new CellRangeAddress(9, 9, 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }

    private void createSectionTwo(final Sheet sheet, final List<QuarterlyReportDTO> reports) {
        Row row = sheet.createRow(11);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("II.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Reporting Period");
        row = sheet.createRow(12);
        cell = createCell(row, 1);
        cell.setCellValue("This report relates to the following reporting period.");
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 1, 3));

        row = sheet.createRow(13);
        cell = createCell(row, 1);
        //calculate the minimum start date and maximum end dates out of all reports passed in
        Date minDate = null;
        Date maxDate = null;
        for (QuarterlyReportDTO report : reports) {
            if (minDate == null || report.getStartDate().getTime() < minDate.getTime()) {
                minDate = report.getStartDate();
            }
            if (maxDate == null || report.getEndDate().getTime() > maxDate.getTime()) {
                maxDate = report.getEndDate();
            }
        }
        DateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy");
        cell.setCellValue(dateFormatter.format(minDate) + " through " + dateFormatter.format(maxDate));
        pt.drawBorders(new CellRangeAddress(13, 13, 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }

    private void createSectionThree(final Sheet sheet, final List<QuarterlyReportDTO> reports) {
        Row row = sheet.createRow(15);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("III.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Surveillance Activities and Outcomes");
        row = sheet.createRow(16);
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("The ONC-ACB used the following selection method to make its "
                + "random selection of certified Complete EHRs and certified Health IT "
                + "Modules for surveillance initiated during the reporting period.");
        sheet.addMergedRegion(new CellRangeAddress(16, 16, 1, 3));

        row = sheet.createRow(17);
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        cell.getCellStyle().setVerticalAlignment(VerticalAlignment.TOP);
        //TODO: calculate row height based on the length of the text filling up
        //the width of the cells and wrapping; height of 3 lines per report is the minimum
        row.setHeightInPoints((3*reports.size()*sheet.getDefaultRowHeightInPoints()));
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getActivitiesOutcomesSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                buf.append(report.getQuarter().getName()).append(":")
                    .append(report.getActivitiesOutcomesSummary())
                    .append("\n");
            }
            cell.setCellValue(buf.toString());
        }
        sheet.addMergedRegion(new CellRangeAddress(17, 17, 1, 3));
        pt.drawBorders(new CellRangeAddress(17, 17, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.ALL);

        row = sheet.createRow(19);
        cell = createCell(row, 1);
        cell.setCellValue("Please log the surveillance activities and their outcomes to "
                + "the \"Activities and Outcomes\" sheet of this workbook.");
        sheet.addMergedRegion(new CellRangeAddress(19, 19, 1, 3));
    }

    private void createSectionFour(final Sheet sheet, final List<QuarterlyReportDTO> reports) {
        Row row = sheet.createRow(21);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("IV.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Sampling and Selecting");
        row = sheet.createRow(22);
        cell = createCell(row, 1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Exclusion and Exhaustion");
        row = sheet.createRow(23);
        cell = createCell(row, 1);
        cell.setCellValue("The following certified Complete EHRs and certified "
                + "Health IT Modules were excluded from randomized surveillance for the reasons stated below.");
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 1, 3));

        //this is the beginning of a big table
        row = sheet.createRow(25);
        cell = createCell(row, 1);
        cell.setCellStyle(tableHeadingStyle);
        cell.setCellValue("Complete EHR or Health IT Module (CHPL ID)");
        cell = createCell(row, 2);
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
        cell = createCell(row, 1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Reactive Surveillance");
        row = sheet.createRow(38);
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        row.setHeightInPoints((3*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Complete EHR or certified "
                + "Health IT Module. ");
        sheet.addMergedRegion(new CellRangeAddress(38, 38, 1, 3));
        row = sheet.createRow(39);
        //TODO: can we figure out the row height actually needed for the text provided?
        row.setHeightInPoints((10*reports.size()*sheet.getDefaultRowHeightInPoints()));
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getReactiveSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                buf.append(report.getQuarter().getName()).append(":")
                    .append(report.getReactiveSummary())
                    .append("\n");
            }
            cell.setCellValue(buf.toString());
        }
        pt.drawBorders(new CellRangeAddress(39, 39, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(39, 39, 1, 3));
    }

    private void createSectionFive(final Sheet sheet, final List<QuarterlyReportDTO> reports) {
        Row row = sheet.createRow(41);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("V.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Prioritized Surveillance");
        row = sheet.createRow(42);
        cell = createCell(row, 1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Prioritized Elements");
        row = sheet.createRow(43);
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        cell.setCellValue("The ONC-ACB undertook the following activities and implemented the "
                + "following measures to evaluate and address the prioritized elements of "
                + "surveillance referred to in Program Policy Guidance #15-01A (November 2015).");
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(43, 43, 1, 3));
        row = sheet.createRow(45);
        //TODO: can we figure out the row height actually needed for the text provided?
        row.setHeightInPoints((10*reports.size()*sheet.getDefaultRowHeightInPoints()));
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getPrioritizedElementSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                buf.append(report.getQuarter().getName()).append(":")
                    .append(report.getPrioritizedElementSummary())
                    .append("\n");
            }
            cell.setCellValue(buf.toString());
        }
        pt.drawBorders(new CellRangeAddress(45, 45, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(45, 45, 1, 3));

        row = sheet.createRow(47);
        cell = createCell(row, 1);
        cell.setCellStyle(italicUnderlinedSmallStyle);
        cell.setCellValue("Transparency and Disclosure Requirements");
        row = sheet.createRow(48);
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        cell.setCellValue("The ONC-ACB undertook the following activities and implemented the following measures "
                + "to ensure adherence by developers to transparency and disclosure requirements, as required of "
                + "the ONC-ACB under 45 CFR § 170.523(k):");
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(48, 48, 1, 3));
        row = sheet.createRow(50);
        //TODO: can we figure out the row height actually needed for the text provided?
        row.setHeightInPoints((10*reports.size()*sheet.getDefaultRowHeightInPoints()));
        cell = createCell(row, 1);
        cell.setCellStyle(wrappedStyle);
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getTransparencyDisclosureSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                buf.append(report.getQuarter().getName()).append(":")
                    .append(report.getTransparencyDisclosureSummary())
                    .append("\n");
            }
            cell.setCellValue(buf.toString());
        }
        pt.drawBorders(new CellRangeAddress(50, 50, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(50, 50, 1, 3));
    }

    private void createSectionSix(final Sheet sheet) {
        Row row = sheet.createRow(52);
        Cell cell = createCell(row, 0);
        cell.setCellStyle(sectionNumberingStyle);
        cell.setCellValue("VI.");
        cell = createCell(row, 1);
        cell.setCellStyle(sectionHeadingStyle);
        cell.setCellValue("Complaints Reporting");
        row = sheet.createRow(53);
        cell = createCell(row, 1);
        cell.setCellValue("Please log the complaints and any actions to the \"Complaints\" sheet of this workbook.");
    }
}
