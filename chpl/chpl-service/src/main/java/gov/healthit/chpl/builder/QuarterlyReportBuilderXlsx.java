package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

@Component
public class QuarterlyReportBuilderXlsx {

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(QuarterlyReportDTO report) throws IOException {
        List<QuarterlyReportDTO> reports = new ArrayList<QuarterlyReportDTO>();
        reports.add(report);

        Workbook workbook = XSSFWorkbookFactory.create(true);
        ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
        reportInfoBuilder.buildWorksheet(reports);
        ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                new ActivitiesAndOutcomesWorksheetBuilder(workbook);
        activitiesAndOutcomesBuilder.buildWorksheet();
        createComplaintsWorksheet(workbook);
        SurveillanceSummaryWorksheetBuilder survSummaryBuilder = new SurveillanceSummaryWorksheetBuilder(workbook);
        survSummaryBuilder.buildWorksheet();
        return workbook;
    }

    private void createComplaintsWorksheet(final Workbook workbook) {
        workbook.createSheet("Complaints");
    }
}
