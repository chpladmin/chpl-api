package gov.healthit.chpl.surveillance.report.builder;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public abstract class ReportInfoWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 6;
    private static final int MIN_TEXT_AREA_LINES = 4;
    private int lastDataRow;
    protected PropertyTemplate pt;

    protected abstract int addExclusionAndExhaustionSection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow);

    protected abstract String getReportingAcbDescription();

    protected abstract String getSurveillanceActivitiesAndOutcomesDescription();

    protected abstract String getReactiveSummaryTitle();

    protected abstract String getReactiveSummaryDescription();

    protected abstract String getDisclosureSummaryTitle();

    protected abstract String getDisclosureSummaryDescription();

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return lastDataRow;
    }

    protected int createHeader(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getBoldStyle());
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) "
                + determineYear(reports)
                + (reports != null && reports.size() == 1 ? " Quarterly " : " Annual ")
                + "Surveillance Report");
        return row.getRowNum() + 1;
    }

    public Sheet buildWorksheet(SurveillanceReportWorkbookWrapper workbook, List<QuarterlyReportDTO> reports) throws IOException {
        lastDataRow = 0;
        pt = new PropertyTemplate();
        //create sheet or get the sheet if it already exists
        Sheet sheet = workbook.getSheet("Report Information", new Color(141, 180, 226), getLastDataColumn());

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //columns B, C, and D need a certain width to get word wrap right
        sheet.setColumnWidth(1, workbook.getColumnWidth(50.67));
        sheet.setColumnWidth(2,  workbook.getColumnWidth(42));
        sheet.setColumnWidth(3, workbook.getColumnWidth(42));

        int nextRow = 1;
        nextRow = createHeader(workbook, sheet, reports, nextRow) + 1;
        nextRow = createAcbSection(workbook, sheet, reports, nextRow) + 1;
        nextRow = createReportingPeriodSection(workbook, sheet, reports, nextRow) + 1;
        nextRow = createActivitiesAndOutcomesSection(workbook, sheet, reports, nextRow) + 1;
        nextRow = createSelectingAndSamplingSection(workbook, sheet, reports, nextRow) + 1;
        nextRow = createPrioritizedSurveillanceSection(workbook, sheet, reports, nextRow) + 1;
        lastDataRow = createComplaintsSection(workbook, sheet, nextRow) + 1;

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);

        return sheet;
    }

    private int createAcbSection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, List<QuarterlyReportDTO> reports,
            int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("I.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Reporting ONC-ACB");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
        cell.setCellValue(getReportingAcbDescription());
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
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
        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
        return row.getRowNum()+1;
    }

    private int createReportingPeriodSection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
             List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("II.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Reporting Period");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
        cell.setCellValue("This report relates to the following reporting period.");
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
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
        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);
        return row.getRowNum()+1;
    }

    private int createActivitiesAndOutcomesSection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("III.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Surveillance Activities and Outcomes");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue(getSurveillanceActivitiesAndOutcomesDescription());
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getActivitiesOutcomesSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                if (!StringUtils.isEmpty(report.getActivitiesOutcomesSummary())) {
                    buf.append(report.getQuarter().getName()).append(":")
                        .append(report.getActivitiesOutcomesSummary())
                        .append("\n");
                }
            }
            cell.setCellValue(buf.toString());
        }
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        int lineCount = workbook.calculateLineCount(cell.getStringCellValue(), sheet, 1, 3);
        row.setHeightInPoints((Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));

        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3),
                BorderStyle.MEDIUM, BorderExtent.ALL);

        //skip a row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
        cell.setCellValue("Please log the surveillance activities and their outcomes to "
                + "the \"Activities and Outcomes\" sheet of this workbook.");
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        return row.getRowNum()+1;
    }

    private int createSelectingAndSamplingSection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("IV.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Sampling and Selecting");
        currRow = addExclusionAndExhaustionSection(workbook, sheet, reports, currRow);
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getReactiveSummaryTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        row.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue(getReactiveSummaryDescription());
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());

        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getReactiveSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                if (!StringUtils.isEmpty(report.getReactiveSummary())) {
                    if (buf.length() > 0) {
                        buf.append("\n");
                    }
                    buf.append(report.getQuarter().getName()).append(":")
                        .append(report.getReactiveSummary());
                }
            }
            cell.setCellValue(buf.toString());
        }
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        int lineCount = workbook.calculateLineCount(cell.getStringCellValue(), sheet, 1, 3);
        row.setHeightInPoints((Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));
        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        return row.getRowNum()+1;
    }

    private int createPrioritizedSurveillanceSection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("V.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Prioritized Surveillance");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue("Prioritized Elements");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue("The ONC-ACB undertook the following activities and implemented the "
                + "following measures to evaluate and address the prioritized elements of "
                + "surveillance referred to in Program Policy Resource #18-03 (October 5, 2018).");
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getPrioritizedElementSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                if (!StringUtils.isEmpty(report.getPrioritizedElementSummary())) {
                    buf.append(report.getQuarter().getName()).append(":")
                        .append(report.getPrioritizedElementSummary())
                        .append("\n");
                }
            }
            cell.setCellValue(buf.toString());
        }
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        int lineCount = workbook.calculateLineCount(cell.getStringCellValue(), sheet, 1, 3);
        row.setHeightInPoints((Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));
        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getDisclosureSummaryTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue(getDisclosureSummaryDescription());
        row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getDisclosureSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                if (!StringUtils.isEmpty(report.getDisclosureSummary())) {
                    buf.append(report.getQuarter().getName()).append(":")
                        .append(report.getDisclosureSummary())
                        .append("\n");
                }
            }
            cell.setCellValue(buf.toString());
        }
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        lineCount = workbook.calculateLineCount(cell.getStringCellValue(), sheet, 1, 3);
        row.setHeightInPoints((Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));
        pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        return row.getRowNum()+1;
    }

    private int createComplaintsSection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 0, workbook.getSectionNumberingStyle());
        cell.setCellValue("VI.");
        cell = workbook.createCell(row, 1, workbook.getSectionHeadingStyle());
        cell.setCellValue("Complaints Reporting");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
        cell.setCellValue("Please log the complaints and any actions to the \"Complaints\" sheet of this workbook.");
        return row.getRowNum()+1;
    }

    protected int determineYear(List<QuarterlyReportDTO> quarterlyReports) {
        return quarterlyReports.get(0).getYear();
    }
}
