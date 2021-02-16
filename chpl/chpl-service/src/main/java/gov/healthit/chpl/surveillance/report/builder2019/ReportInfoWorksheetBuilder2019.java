package gov.healthit.chpl.surveillance.report.builder2019;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.ReportInfoWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;

public class ReportInfoWorksheetBuilder2019 extends ReportInfoWorksheetBuilder {
    private static final int MIN_EXCLUSION_LINES = 1;
    private SurveillanceReportManager reportManager;
    private PropertyTemplate pt;

    public ReportInfoWorksheetBuilder2019(SurveillanceReportManager reportManager) {
        super();
        this.reportManager = reportManager;
    }

    @Override
    protected String getReportingAcbDescription() {
        return "This report is submitted by the below named ONC-ACB in "
                + "accordance with 45 CFR § 170.523(i)(2) and 45 CFR § 170.556(e).";
    }

    @Override
    protected String getSurveillanceActivitiesAndOutcomesDescription() {
        return "The ONC-ACB used the following selection method to make its "
                + "random selection of certified Complete EHRs and certified Health IT "
                + "Modules for surveillance initiated during the reporting period.";
    }

    @Override
    protected String getReactiveSummaryDescription() {
        return "In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Complete EHR or certified "
                + "Health IT Module.";
    }

    @Override
    protected String getDisclosureSummaryTitle() {
        return "Transparency and Disclosure Requirements";
    }

    @Override
    protected String getDisclosureSummaryDescription() {
        return "The ONC-ACB undertook the following activities and implemented the following measures "
                + "to ensure adherence by developers to transparency and disclosure requirements, as required of "
                + "the ONC-ACB under 45 CFR § 170.523(k):";
    }

    @Override
    protected String getReactiveSummaryTitle() {
        return "Reactive Surveillance";
    }


    @Override
    protected int addExclusionAndExhaustionSection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
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
        return currRow;
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
