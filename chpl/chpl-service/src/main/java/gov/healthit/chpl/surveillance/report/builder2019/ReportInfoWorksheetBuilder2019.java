package gov.healthit.chpl.surveillance.report.builder2019;

import java.util.List;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.builder.ReportInfoWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ReportInfoWorksheetBuilder2019 extends ReportInfoWorksheetBuilder {

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

         //draw border around the table, including the heading row
        pt.drawBorders(new CellRangeAddress(currRow - 1, row.getRowNum(), 1, 2),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        //skip a row after the table
        currRow++;
        return currRow;
    }
}
