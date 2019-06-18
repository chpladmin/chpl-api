package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

public class QuarterlyReportBuilderXlsx {
    public QuarterlyReportBuilderXlsx() {
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final QuarterlyReportDTO report,
            final List<CertifiedProductSearchDetails> relevantListingDetails) throws IOException {
        List<QuarterlyReportDTO> reports = new ArrayList<QuarterlyReportDTO>();
        reports.add(report);

        Workbook workbook = XSSFWorkbookFactory.create(true);
        ListWorksheetBuilder listBuilder = new ListWorksheetBuilder(workbook);
        listBuilder.buildWorksheet();
        ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
        reportInfoBuilder.buildWorksheet(reports);
        ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                new ActivitiesAndOutcomesWorksheetBuilder(workbook);
        Map<QuarterlyReportDTO, List<CertifiedProductSearchDetails>> reportListingMap =
                new HashMap<QuarterlyReportDTO, List<CertifiedProductSearchDetails>>();
        reportListingMap.put(report, relevantListingDetails);
        activitiesAndOutcomesBuilder.buildWorksheet(reportListingMap);
        createComplaintsWorksheet(workbook);
        SurveillanceSummaryWorksheetBuilder survSummaryBuilder = new SurveillanceSummaryWorksheetBuilder(workbook);
        survSummaryBuilder.buildWorksheet();

        //hide the ListSheet
        workbook.setSheetHidden(0, true);
        workbook.setActiveSheet(1);
        return workbook;
    }

    private void createComplaintsWorksheet(final Workbook workbook) {
        workbook.createSheet("Complaints");
    }
}
