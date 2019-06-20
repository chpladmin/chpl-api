package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
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
            quarterlyReports.sort(new Comparator<QuarterlyReportDTO>() {
                @Override
                public int compare(final QuarterlyReportDTO o1, final QuarterlyReportDTO o2) {
                    if (o1.getStartDate() == null || o2.getStartDate() == null) {
                        return 0;
                    }
                    if (o1.getStartDate().getTime() < o2.getStartDate().getTime()) {
                        return -1;
                    }
                    if (o1.getStartDate().getTime() > o2.getStartDate().getTime()) {
                        return 1;
                    }
                    return 0;
                }
            });

            ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
            reportInfoBuilder.buildWorksheet(quarterlyReports);
            ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                    new ActivitiesAndOutcomesWorksheetBuilder(workbook);
            activitiesAndOutcomesBuilder.buildWorksheet(quarterlyReports, relevantListings);
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
