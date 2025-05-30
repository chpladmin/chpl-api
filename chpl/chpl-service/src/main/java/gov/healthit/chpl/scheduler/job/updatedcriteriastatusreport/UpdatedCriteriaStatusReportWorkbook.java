package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;

@Component
public class UpdatedCriteriaStatusReportWorkbook extends UpdatedCriteriaSpreadsheetBase {
    public static final Integer TOTAL_NUMBER_OF_MONTHS = 12;

    private UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet;
    private String template;
    private CertificationCriteriaManager criteriaManager;
    private CertificationCriterionComparator certificationCriterionComparator;
    private ReportDateService reportDateService;
    private Environment env;

    public UpdatedCriteriaStatusReportWorkbook(@Value("${updatedCriteriaStatusReportTemplate}") String template,
            UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet,
            CertificationCriteriaManager criteriaManager,
            CertificationCriterionComparator certificationCriterionComparator,
            ReportDateService reportDateService,
            Environment env) {
        this.template = template;
        this.updatedCriteriaStatusReportSheet = updatedCriteriaStatusReportSheet;
        this.criteriaManager = criteriaManager;
        this.certificationCriterionComparator = certificationCriterionComparator;
        this.reportDateService = reportDateService;
        this.env = env;
    }

    public File generateSpreadsheet() throws IOException {
        File newFile = copyTemplateFileToTemporaryFile(template, getFilename());
        Workbook workbook = getWorkbook(newFile);
        List<LocalDate> allReportDates = calculateAllMonthsOfReportDatesBasedOnAvailableData();

        criteriaManager.getActiveToday().stream()
                .sorted(certificationCriterionComparator)
                .forEach(crit ->  updatedCriteriaStatusReportSheet.generateSheetForCriteriaOnDates(crit, allReportDates, workbook));

        //Remove the template sheet
        workbook.removeSheetAt(0);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.fileName").toString();
    }

    private List<LocalDate> calculateAllMonthsOfReportDatesBasedOnAvailableData() {
        List<LocalDate> allReportDates = new ArrayList<LocalDate>();
        LocalDate preferredReportDay = LocalDate.now();
        for (int i = TOTAL_NUMBER_OF_MONTHS; i >= 1; --i) {
            LocalDate actualReportDay = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(preferredReportDay);
            allReportDates.add(actualReportDay);
            preferredReportDay = actualReportDay.minusMonths(1);
        }
        return allReportDates;
    }
}
