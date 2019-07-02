package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

@Component("quarterlyReportBuilder")
public class QuarterlyReportBuilderXlsx {

    private ListWorksheetBuilder listWorksheetBuilder;
    private ReportInfoWorksheetBuilder reportInfoWorksheetBuilder;
    private ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder;
    private ComplaintsWorksheetBuilder complaintsWorksheetBuilder;
    private SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder;

    @Autowired
    public QuarterlyReportBuilderXlsx(final ListWorksheetBuilder listWorksheetBuilder,
            final ReportInfoWorksheetBuilder reportInfoWorksheetBuilder,
            final ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesWorksheetBuilder,
            final ComplaintsWorksheetBuilder complaintsWorksheetBuilder,
            final SurveillanceSummaryWorksheetBuilder survSummaryWorksheetBuilder) {
        this.listWorksheetBuilder = listWorksheetBuilder;
        this.reportInfoWorksheetBuilder = reportInfoWorksheetBuilder;
        this.activitiesAndOutcomesWorksheetBuilder = activitiesAndOutcomesWorksheetBuilder;
        this.complaintsWorksheetBuilder = complaintsWorksheetBuilder;
        this.survSummaryWorksheetBuilder = survSummaryWorksheetBuilder;
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final QuarterlyReportDTO report) throws IOException {
        List<QuarterlyReportDTO> reports = new ArrayList<QuarterlyReportDTO>();
        reports.add(report);

        Workbook workbook = XSSFWorkbookFactory.create(true);
        listWorksheetBuilder.setWorkbook(workbook);
        listWorksheetBuilder.buildWorksheet();
        reportInfoWorksheetBuilder.setWorkbook(workbook);
        reportInfoWorksheetBuilder.buildWorksheet(reports);
        activitiesAndOutcomesWorksheetBuilder.setWorkbook(workbook);
        activitiesAndOutcomesWorksheetBuilder.buildWorksheet(reports);
        complaintsWorksheetBuilder.setWorkbook(workbook);
        complaintsWorksheetBuilder.buildWorksheet(reports);
        survSummaryWorksheetBuilder.setWorkbook(workbook);
        survSummaryWorksheetBuilder.buildWorksheet();

        //hide the ListSheet
        workbook.setSheetHidden(0, true);
        workbook.setActiveSheet(1);
        return workbook;
    }
}
