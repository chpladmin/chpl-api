package gov.healthit.chpl.surveillance.report.builder2019;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;

/**
 * Creates a worksheet with high level information about the report.
 *
 */
public abstract class ReportInfoWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 6;
    private static final int MIN_TEXT_AREA_LINES = 4;
    private static final int MIN_EXCLUSION_LINES = 1;
    private SurveillanceReportManager reportManager;
    private PropertyTemplate pt;
    private int lastDataRow;

    public ReportInfoWorksheetBuilder(SurveillanceReportManager reportManager) {
        this.reportManager = reportManager;
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return lastDataRow;
    }

    /**
     * Creates a formatted Excel worksheet with the information in the reports.
     * @param report
     * @return
     */
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

    protected abstract int createHeader(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow);

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
        cell.setCellValue("This report is submitted by the below named ONC-ACB in "
                + "accordance with 45 CFR § 170.523(i)(2) and 45 CFR § 170.556(e).");
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
        cell.setCellValue("The ONC-ACB used the following selection method to make its "
                + "random selection of certified Complete EHRs and certified Health IT "
                + "Modules for surveillance initiated during the reporting period.");
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
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue("Exclusion and Exhaustion");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1);
        cell.setCellValue("The following certified Complete EHRs and certified "
                + "Health IT Modules were excluded from randomized surveillance for the reasons stated below.");
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //this is the beginning of a big table
        //skip a row on purpose
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getLeftAlignedTableHeadingStyle());
        cell.setCellValue("Complete EHR or Health IT Module (CHPL ID)");
        cell = workbook.createCell(row, 2, workbook.getLeftAlignedTableHeadingStyle());
        cell.setCellValue("Reason(s) for Exclusion");

        LinkedHashMap<String, List<QuarterlyExclusionReason>> combinedExclusions =
                new LinkedHashMap<String, List<QuarterlyExclusionReason>>();
        //Get the excluded listings for each quarterly report
        //put them in a data structure we can use to write out to the table.
        //Using a linked hash map to maintain insertion order.. also only doing the map thing
        //in case the same listing is excluded across multiple quarters so we can combine it in the printed table.
        for (QuarterlyReportDTO report : reports) {
            List<QuarterlyReportRelevantListingDTO> relevantListings = reportManager.getRelevantListings(report);
            for (QuarterlyReportRelevantListingDTO relevantListing : relevantListings) {
                if (relevantListing.isExcluded()) {
                    QuarterlyExclusionReason reason =
                            new QuarterlyExclusionReason(report.getQuarter().getName(), relevantListing.getExclusionReason());
                    //look to see if there's already an entry for this exclusion
                    if (combinedExclusions.get(relevantListing.getChplProductNumber()) != null) {
                        combinedExclusions.get(relevantListing.getChplProductNumber()).add(reason);
                    } else {
                        List<QuarterlyExclusionReason> reasons = new ArrayList<QuarterlyExclusionReason>();
                        reasons.add(reason);
                        combinedExclusions.put(relevantListing.getChplProductNumber(), reasons);
                    }
                }
            }
        }

        int tableStartRow = currRow;
        for (String chplNumber : combinedExclusions.keySet()) {
            row = workbook.getRow(sheet, currRow++);
            cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
            cell.setCellValue(chplNumber);
            cell = workbook.createCell(row, 2, workbook.getTopAlignedWrappedStyle());
            List<QuarterlyExclusionReason> reasons = combinedExclusions.get(chplNumber);
            if (reasons != null && reasons.size() == 1) {
                if (reports.size() > 1) {
                    cell.setCellValue(reasons.get(0).getQuarterName() + ": " + reasons.get(0).getReason().trim());
                } else {
                    cell.setCellValue(reasons.get(0).getReason().trim());
                }
            } else if (reasons != null && reasons.size() > 1) {
                StringBuffer buf = new StringBuffer();
                for (QuarterlyExclusionReason reason : combinedExclusions.get(chplNumber)) {
                    if (buf.length() > 0) {
                        buf.append("\n");
                    }
                    buf.append(reason.toString());
                }
                String value = buf.toString().trim();
                cell.setCellValue(value);
            }
            int lineCount = workbook.calculateLineCount(cell.getStringCellValue(), sheet, 2, 2);
            row.setHeightInPoints((Math.max(MIN_EXCLUSION_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));
            pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 2),
                BorderStyle.THIN, BorderExtent.TOP);
        }
         //draw border around the table, including the heading row
        pt.drawBorders(new CellRangeAddress(tableStartRow - 1, row.getRowNum(), 1, 2),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        //skip a row after the table
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue("Reactive Surveillance");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        row.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue("In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Complete EHR or certified "
                + "Health IT Module. ");
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
        cell.setCellValue("Transparency and Disclosure Requirements");
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue("The ONC-ACB undertook the following activities and implemented the following measures "
                + "to ensure adherence by developers to transparency and disclosure requirements, as required of "
                + "the ONC-ACB under 45 CFR § 170.523(k):");
        row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getTransparencyDisclosureSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReportDTO report : reports) {
                if (!StringUtils.isEmpty(report.getTransparencyDisclosureSummary())) {
                    buf.append(report.getQuarter().getName()).append(":")
                        .append(report.getTransparencyDisclosureSummary())
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

    private class QuarterlyExclusionReason {
        private String quarterName;
        private String reason;
        public QuarterlyExclusionReason() {
        }
        public QuarterlyExclusionReason(String quarterName, String reason) {
            this.quarterName = quarterName;
            this.reason = reason;
        }
        public String getQuarterName() {
            return quarterName;
        }
        public void setQuarterName(String quarterName) {
            this.quarterName = quarterName;
        }
        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }
        public String toString() {
            return this.quarterName + ": " + reason;
        }
    }
}
