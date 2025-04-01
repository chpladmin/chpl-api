package gov.healthit.chpl.surveillance.report.builder2025;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.builder.ReportInfoWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ReportInfoWorksheetBuilder2025 extends ReportInfoWorksheetBuilder {

    @Override
    protected String getReportingAcbDescription() {
        return "This report is submitted by the below ONC-ACB in accordance with "
                + "45 CFR § 170.523(i)(2), 45 CFR § 170.523(n), and 45 CFR § 170.556(e).";
    }

    @Override
    protected String getRandomizedSurveillanceActivitiesAndOutcomesTitle() {
        return "Randomized Surveillance – Selection Methods";
    }

    @Override
    protected String getRandomizedSurveillanceActivitiesAndOutcomesDescription() {
        return "The ONC-ACB used the following selection method to make its random "
                + "selection of certified Health IT Modules for surveillance initiated "
                + "during the reporting period.";
    }

    @Override
    protected String getAllSurveillanceActivitiesAndOutcomesTitle() {
        return "All Surveillance Activities and Outcomes";
    }

    @Override
    protected String getReactiveSummaryTitle() {
        return "Reactive Surveillance Summary";
    }

    @Override
    protected String getReactiveSummaryDescription() {
        return "In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Health IT Module.";
    }

    @Override
    protected String getIcsSummaryTitle() {
        return "ICS Surveillance Summary";
    }

    @Override
    protected String getIcsSummaryDescription() {
        return "In order to meet requirements to conduct reactive surveillance on listings with multiple ICS "
                + "requests, the ONC-ACB conducted the following ICS related surveillance. Please outline the "
                + "number of ICS-related surveillances conducted, the method to surveil these products and "
                + "the approach to include prioritized elements as outlined in the Surveillance Resource. ";
    }

    @Override
    protected int createIcsSurveillanceSubsection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReport> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getIcsSummaryTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        row.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
        cell.setCellValue(getIcsSummaryDescription());
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());

        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getIcsSurveillanceSummary());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReport report : reports) {
                if (!StringUtils.isEmpty(report.getIcsSurveillanceSummary())) {
                    if (buf.length() > 0) {
                        buf.append("\n");
                    }
                    buf.append(report.getQuarter()).append(":")
                        .append(report.getIcsSurveillanceSummary());
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
        return row.getRowNum() + 1;
    }

    @Override
    protected String getPrioritizedSurveillanceDescription() {
        return "The ONC-ACB undertook the following activities and implemented the following measures "
                + "to evaluate and address the prioritized elements of surveillance referred to in Program Policy "
                + "Resource #18-03 (October 5, 2018).";
    }

    @Override
    protected String getPrioritizedCriteriaTitle() {
        return "Prioritized Criteria";
    }

    @Override
    protected String getPrioritizedCriteriaDescription() {
        return "Please describe which prioritized criteria were surveilled, how and with what frequency. "
                + "Summarize the approach taken to conduct surveillance on these prioritized criteria.";
    }

    @Override
    protected String getDisclosureSummaryTitle() {
        return "Disclosure Requirements Summary";
    }

    @Override
    protected String getDisclosureSummaryDescription() {
        return "The ONC-ACB undertook the following activities and implemented the following measures to "
                + "ensure adherence by developers to disclose additional types of costs or fees requirements, "
                + "as required of the ONC-ACB under 45 CFR § 170.523(k):";
    }

    protected String getDeveloperComplaintsLogReviewTitle() {
        return "Developer Complaints Log Review";
    }

    protected String getDeveloperComplaintsLogReviewDescription() {
        return "Describe the activities conducted in the past quarter related to the review of developers' complaints logs. "
                + "In your description be sure to discuss the extent to which the developer followed its internal complaints "
                + "process and any deficiencies with its process. Please also indicate the frequency of complaints that the "
                + "developer received that are associated with each of the prioritized elements as specified by "
                + "ONC/ASTP. Additional insights on individual findings can be included in the Surveillance Activities "
                + "and Outcomes under \"Surveillance Findings\". ";
    }

    @Override
    protected int createDeveloperComplaintsLogReviewSubsection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReport> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getDeveloperComplaintsLogReviewTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue(getDeveloperComplaintsLogReviewDescription());
        row.setHeightInPoints((4 * sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getDeveloperComplaintsLogReview());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReport report : reports) {
                if (!StringUtils.isEmpty(report.getDeveloperComplaintsLogReview())) {
                    buf.append(report.getQuarter()).append(":")
                        .append(report.getDeveloperComplaintsLogReview())
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
        return row.getRowNum() + 1;
    }

    protected String getPostCertificationPerformanceTitle() {
        return "Post-certification Performance of Certified Capabilities ";
    }

    protected String getPostCertificationPerformanceDescription() {
        return "The assessment of potential non-conformities resulting from implementation or business practices "
                + "of a developer that could affect the performance of certified capabilities in the field.";
    }

    @Override
    protected int createPostCertificationPerformanceSubsection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReport> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getPostCertificationPerformanceTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue(getPostCertificationPerformanceDescription());
        row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getPostCertificationPerformanceOfCertifiedCapabilities());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReport report : reports) {
                if (!StringUtils.isEmpty(report.getPostCertificationPerformanceOfCertifiedCapabilities())) {
                    buf.append(report.getQuarter()).append(":")
                        .append(report.getPostCertificationPerformanceOfCertifiedCapabilities())
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
        return row.getRowNum() + 1;
    }

    protected String getAppropriateUseOfMarkTitle() {
        return "Appropriate Use of Mark";
    }

    protected String getAppropriateUseOfMarkDescription() {
        return "Describe activities and frequency of assessment of the appropriate use of the ONC Health IT Certification "
                + "and Design Mark on developer public-facing materials.";
    }

    @Override
    protected int createAppropriateUseOfMarkSubsection(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReport> reports, int beginRow) {

        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getItalicUnderlinedSmallStyle());
        cell.setCellValue(getAppropriateUseOfMarkTitle());
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        cell.setCellValue(getAppropriateUseOfMarkDescription());
        row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 3));

        //skip row
        currRow++;
        row = workbook.getRow(sheet, currRow++);
        cell = workbook.createCell(row, 1, workbook.getTopAlignedWrappedStyle());
        if (reports.size() == 1) {
            cell.setCellValue(reports.get(0).getAppropriateUseOfMark());
        } else {
            StringBuffer buf = new StringBuffer();
            for (QuarterlyReport report : reports) {
                if (!StringUtils.isEmpty(report.getAppropriateUseOfMark())) {
                    buf.append(report.getQuarter()).append(":")
                        .append(report.getAppropriateUseOfMark())
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
        return row.getRowNum() + 1;
    }

    @Override
    protected String getComplaintsReportingTitle() {
        return "Complaints Reported to ONC-ACB";
    }
}
