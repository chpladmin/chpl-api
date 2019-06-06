package gov.healthit.chpl.builder;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;

@Component
public class AnnualReportBuilderXlsx {

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final AnnualReportDTO report) throws IOException {
        Workbook workbook = XSSFWorkbookFactory.create(true);
        createSurveillanceSummaryWorksheet(workbook);
        SurveillanceExperienceWorksheetBuilder builder = new SurveillanceExperienceWorksheetBuilder(workbook);
        builder.buildWorksheet(report);
        return workbook;
    }

    private void createSurveillanceSummaryWorksheet(final Workbook workbook) {
        workbook.createSheet("Surveillance Summary");
    }
}
