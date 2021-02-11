package gov.healthit.chpl.surveillance.report.builder2019;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

@Component("quarterlyReportBuilder")
public class QuarterlyReportBuilderXlsx {

    private ListWorksheetBuilder listWorksheetBuilder;
    private ReportInfoWorksheetBuilder reportInfoWorksheetBuilder;
    private ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder;
    private ComplaintsWorksheetBuilder complaintsWorksheetBuilder;
    private SurveillanceSummaryWorksheetBuilder summaryWorksheetBuilder;

    @Autowired
    public QuarterlyReportBuilderXlsx(ListWorksheetBuilder listWorksheetBuilder,
            QuarterlyReportInfoWorksheetBuilder reportInfoWorksheetBuilder,
            ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder,
            ComplaintsWorksheetBuilder complaintsWorksheetBuilder,
            SurveillanceSummaryWorksheetBuilder summaryWorksheetBuilder) {
        this.listWorksheetBuilder = listWorksheetBuilder;
        this.reportInfoWorksheetBuilder = reportInfoWorksheetBuilder;
        this.activitiesAndOutcomesWorksheetBuilder = activitiesAndOutcomesWorksheetBuilder;
        this.complaintsWorksheetBuilder = complaintsWorksheetBuilder;
        this.summaryWorksheetBuilder = summaryWorksheetBuilder;
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(QuarterlyReportDTO report) throws IOException {
        SurveillanceReportWorkbookWrapper workbook = new SurveillanceReportWorkbookWrapper();

        List<QuarterlyReportDTO> reports = new ArrayList<QuarterlyReportDTO>();
        reports.add(report);

        listWorksheetBuilder.buildWorksheet(workbook);
        reportInfoWorksheetBuilder.buildWorksheet(workbook, reports);
        activitiesAndOutcomesWorksheetBuilder.buildWorksheet(workbook, reports);
        complaintsWorksheetBuilder.buildWorksheet(workbook, reports);
        summaryWorksheetBuilder.buildWorksheet(workbook, reports);

        //hide the ListSheet
        workbook.getWorkbook().setSheetHidden(0, true);
        workbook.getWorkbook().setActiveSheet(1);
        return workbook.getWorkbook();
    }
}
