package gov.healthit.chpl.surveillance.report.builder2019;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.AnnualReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.ComplaintsWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.ListWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceExperienceWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceSummaryWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import lombok.NoArgsConstructor;

@Component("annualReportBuilder2019")
@NoArgsConstructor
public class AnnualReportBuilder2019 implements AnnualReportBuilderXlsx {
    private SurveillanceReportManager reportManager;
    private ListWorksheetBuilder listWorksheetBuilder;
    private ReportInfoWorksheetBuilder2019 reportInfoWorksheetBuilder;
    private ActivitiesAndOutcomesWorksheetBuilder2019 activitiesAndOutcomesWorksheetBuilder;
    private ComplaintsWorksheetBuilder complaintsWorksheetBuilder;
    private SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder;
    private SurveillanceExperienceWorksheetBuilder survExprienceWorksheetBuilder;

    @Autowired
    public AnnualReportBuilder2019(SurveillanceReportManager reportManager,
            ListWorksheetBuilder listWorksheetBuilder,
            ReportInfoWorksheetBuilder2019 reportInfoWorksheetBuilder,
            ActivitiesAndOutcomesWorksheetBuilder2019 activitiesAndOutcomesWorksheetBuilder,
            ComplaintsWorksheetBuilder complaintsWorksheetBuilder,
            SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder,
            SurveillanceExperienceWorksheetBuilder survExprienceWorksheetBuilder) {
        this.reportManager = reportManager;
        this.listWorksheetBuilder = listWorksheetBuilder;
        this.reportInfoWorksheetBuilder = reportInfoWorksheetBuilder;
        this.activitiesAndOutcomesWorksheetBuilder = activitiesAndOutcomesWorksheetBuilder;
        this.complaintsWorksheetBuilder = complaintsWorksheetBuilder;
        this.survSummaryWorksheetBuilder = survSummaryWorksheetBuilder;
        this.survExprienceWorksheetBuilder = survExprienceWorksheetBuilder;
    }

    public Workbook buildXlsx(AnnualReport annualReport) throws IOException {
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
                    return o1.getStartDate().compareTo(o2.getStartDate());
                }
            });
            reportInfoWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
            activitiesAndOutcomesWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
            complaintsWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
            survSummaryWorksheetBuilder.buildWorksheet(workbook, quarterlyReports);
        }
        survExprienceWorksheetBuilder.buildWorksheet(workbook, annualReport);

        //hide the ListSheet
        workbook.getWorkbook().setSheetHidden(0, true);
        workbook.getWorkbook().setActiveSheet(1);
        workbook.close();
        return workbook.getWorkbook();
    }
}
