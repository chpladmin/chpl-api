package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

public class AnnualReportBuilderXlsx {

    public AnnualReportBuilderXlsx() {
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final AnnualReportDTO annualReport,
            final List<QuarterlyReportDTO> quarterlyReports, 
            final List<CertifiedProductSearchDetails> relevantListings) throws IOException {
        Workbook workbook = XSSFWorkbookFactory.create(true);
        ListWorksheetBuilder listBuilder = new ListWorksheetBuilder(workbook);
        listBuilder.buildWorksheet();

        if (quarterlyReports != null && quarterlyReports.size() > 0) {
            //order the quarterly reports by date so they show up in the right order in each sheet
            QuarterlyReportDTO[] sortedQuarterlyReports = quarterlyReports.toArray(new QuarterlyReportDTO[0]);
            Arrays.sort(sortedQuarterlyReports);
            List<QuarterlyReportDTO> sortedQuarterlyReportList = Arrays.asList(sortedQuarterlyReports);

            ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
            reportInfoBuilder.buildWorksheet(sortedQuarterlyReportList);
            ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                    new ActivitiesAndOutcomesWorksheetBuilder(workbook);
            activitiesAndOutcomesBuilder.buildWorksheet(sortedQuarterlyReportList, relevantListings);
            createComplaintsWorksheet(workbook);
        }
        SurveillanceSummaryWorksheetBuilder survSummaryBuilder = new SurveillanceSummaryWorksheetBuilder(workbook);
        survSummaryBuilder.buildWorksheet();
        SurveillanceExperienceWorksheetBuilder builder = new SurveillanceExperienceWorksheetBuilder(workbook);
        builder.buildWorksheet(annualReport);

        //hide the ListSheet
        workbook.setSheetHidden(0, true);
        workbook.setActiveSheet(1);
        return workbook;
    }

    private void createComplaintsWorksheet(final Workbook workbook) {
        workbook.createSheet("Complaints");
    }
}
