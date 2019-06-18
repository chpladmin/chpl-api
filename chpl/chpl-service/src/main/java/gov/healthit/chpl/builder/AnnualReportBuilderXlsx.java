package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.manager.SurveillanceReportManager;

public class AnnualReportBuilderXlsx {

    public AnnualReportBuilderXlsx() {
    }

    /**
     * Creates a formatted Excel document with the information in the report.
     * @param report
     * @return
     */
    public Workbook buildXlsx(final AnnualReportDTO annualReport,
            Map<QuarterlyReportDTO, List<CertifiedProductSearchDetails>> reportListingMap) throws IOException {
        Workbook workbook = XSSFWorkbookFactory.create(true);
        ListWorksheetBuilder listBuilder = new ListWorksheetBuilder(workbook);
        listBuilder.buildWorksheet();

        Set<QuarterlyReportDTO> quarterlyReportSet = reportListingMap.keySet();
        if (quarterlyReportSet != null && quarterlyReportSet.size() > 0) {
            //order the quarterly reports by date so they show up in the right order in each sheet
            QuarterlyReportDTO[] sortedQuarterlyReports = quarterlyReportSet.toArray(new QuarterlyReportDTO[0]);
            Arrays.sort(sortedQuarterlyReports);

            ReportInfoWorksheetBuilder reportInfoBuilder = new ReportInfoWorksheetBuilder(workbook);
            reportInfoBuilder.buildWorksheet(Arrays.asList(sortedQuarterlyReports));
            ActivitiesAndOutcomesWorksheetBuilder activitiesAndOutcomesBuilder =
                    new ActivitiesAndOutcomesWorksheetBuilder(workbook);
            activitiesAndOutcomesBuilder.buildWorksheet(reportListingMap);
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
