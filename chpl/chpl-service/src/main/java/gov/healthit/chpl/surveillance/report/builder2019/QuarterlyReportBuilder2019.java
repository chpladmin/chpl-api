package gov.healthit.chpl.surveillance.report.builder2019;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.builder.ComplaintsWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.ListWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.QuarterlyReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceSummaryWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import lombok.NoArgsConstructor;

@Component("quarterlyReportBuilder2019")
@NoArgsConstructor
public class QuarterlyReportBuilder2019 implements QuarterlyReportBuilderXlsx {

    private ListWorksheetBuilder listWorksheetBuilder;
    private ReportInfoWorksheetBuilder2019 reportInfoWorksheetBuilder;
    private ActivitiesAndOutcomesWorksheetBuilder2019 activitiesAndOutcomesWorksheetBuilder;
    private ComplaintsWorksheetBuilder complaintsWorksheetBuilder;
    private SurveillanceSummaryWorksheetBuilder summaryWorksheetBuilder;

    @Autowired
    public QuarterlyReportBuilder2019(ListWorksheetBuilder listWorksheetBuilder,
            ReportInfoWorksheetBuilder2019 reportInfoWorksheetBuilder,
            ActivitiesAndOutcomesWorksheetBuilder2019 activitiesAndOutcomesWorksheetBuilder,
            ComplaintsWorksheetBuilder complaintsWorksheetBuilder,
            SurveillanceSummaryWorksheetBuilder summaryWorksheetBuilder) {
        this.listWorksheetBuilder = listWorksheetBuilder;
        this.reportInfoWorksheetBuilder = reportInfoWorksheetBuilder;
        this.activitiesAndOutcomesWorksheetBuilder = activitiesAndOutcomesWorksheetBuilder;
        this.complaintsWorksheetBuilder = complaintsWorksheetBuilder;
        this.summaryWorksheetBuilder = summaryWorksheetBuilder;
    }

    public SurveillanceReportWorkbookWrapper buildXlsx(QuarterlyReport report, Logger logger) throws IOException {
        SurveillanceReportWorkbookWrapper workbook = new SurveillanceReportWorkbookWrapper();

        List<QuarterlyReport> reports = new ArrayList<QuarterlyReport>();
        reports.add(report);

        listWorksheetBuilder.buildWorksheet(workbook);
        reportInfoWorksheetBuilder.buildWorksheet(workbook, reports);
        activitiesAndOutcomesWorksheetBuilder.buildWorksheet(workbook, reports, logger);
        complaintsWorksheetBuilder.buildWorksheet(workbook, reports);
        summaryWorksheetBuilder.buildWorksheet(workbook, reports, logger);

        //hide the ListSheet
        workbook.getWorkbook().setSheetHidden(0, true);
        workbook.getWorkbook().setActiveSheet(1);
        return workbook;
    }
}
