package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.manager.SurveillanceReportManager;

@Component("annualReportBuilder")
public class AnnualReportBuilderXlsx {
    private SurveillanceReportManager reportManager;
    private ListWorksheetBuilder listWorksheetBuilder;
    private ReportInfoWorksheetBuilder reportInfoWorksheetBuilder;
    private ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder;
    private ComplaintsWorksheetBuilder complaintsWorksheetBuilder;
    private SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder;
    private SurveillanceExperienceWorksheetBuilder survExprienceWorksheetBuilder;

    @Autowired
    public AnnualReportBuilderXlsx(final SurveillanceReportManager reportManager,
            final ListWorksheetBuilder listWorksheetBuilder,
            final ReportInfoWorksheetBuilder reportInfoWorksheetBuilder,
            final ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder,
            final ComplaintsWorksheetBuilder complaintsWorksheetBuilder,
            final SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder,
            final SurveillanceExperienceWorksheetBuilder survExprienceWorksheetBuilder) {
        this.reportManager = reportManager;
        this.listWorksheetBuilder = listWorksheetBuilder;
        this.reportInfoWorksheetBuilder = reportInfoWorksheetBuilder;
        this.activitiesAndOutcomesWorksheetBuilder = activitiesAndOutcomesWorksheetBuilder;
        this.complaintsWorksheetBuilder = complaintsWorksheetBuilder;
        this.survSummaryWorksheetBuilder = survSummaryWorksheetBuilder;
        this.survExprienceWorksheetBuilder = survExprienceWorksheetBuilder;
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final AnnualReportDTO annualReport) throws IOException {
        SurveillanceReportWorkbookWrapper workbook = new SurveillanceReportWorkbookWrapper();

        listWorksheetBuilder.buildWorksheet(workbook);

        List<QuarterlyReportDTO> quarterlyReports =
                reportManager.getQuarterlyReports(annualReport.getAcb().getId(), annualReport.getYear());
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
            reportInfoWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
            activitiesAndOutcomesWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
            complaintsWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
        }
        survSummaryWorksheetBuilder.buildWorksheet(workbook);
        survExprienceWorksheetBuilder.buildWorksheet(workbook, annualReport);

        //hide the ListSheet
        workbook.getWorkbook().setSheetHidden(0, true);
        workbook.getWorkbook().setActiveSheet(1);
        return workbook.getWorkbook();
    }
}
