package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

@Component
public class AnnualReportBuilderXlsx {
    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    public AnnualReportBuilderXlsx(final QuarterlyReportDAO quarterlyReportDao) {
        this.quarterlyReportDao = quarterlyReportDao;
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final AnnualReportDTO annualReport) throws IOException {
        List<QuarterlyReportDTO> quarterlyReports =
                quarterlyReportDao.getByAcbAndYear(annualReport.getAcb().getId(), annualReport.getYear());

        Workbook workbook = XSSFWorkbookFactory.create(true);
        if (quarterlyReports != null && quarterlyReports.size() > 0) {
            ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
            reportInfoBuilder.buildWorksheet(quarterlyReports);
            ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                    new ActivitiesAndOutcomesWorksheetBuilder(workbook);
            activitiesAndOutcomesBuilder.buildWorksheet();
            createComplaintsWorksheet(workbook);
        }
        SurveillanceSummaryWorksheetBuilder survSummaryBuilder = new SurveillanceSummaryWorksheetBuilder(workbook);
        survSummaryBuilder.buildWorksheet();
        SurveillanceExperienceWorksheetBuilder builder = new SurveillanceExperienceWorksheetBuilder(workbook);
        builder.buildWorksheet(annualReport);
        return workbook;
    }

    private void createComplaintsWorksheet(final Workbook workbook) {
        workbook.createSheet("Complaints");
    }
}
