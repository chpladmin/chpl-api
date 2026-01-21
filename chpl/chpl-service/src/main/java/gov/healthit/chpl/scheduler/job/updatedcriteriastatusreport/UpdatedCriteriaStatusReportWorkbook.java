package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReport;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class UpdatedCriteriaStatusReportWorkbook extends UpdatedCriteriaSpreadsheetBase {
    public static final Integer TOTAL_NUMBER_OF_MONTHS = 12;

    private UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet;
    private String template;
    private CertificationCriteriaManager criteriaManager;
    private CertificationCriterionComparator certificationCriterionComparator;
    private CriteriaUpToDateReportService reportService;
    private Environment env;

    public UpdatedCriteriaStatusReportWorkbook(@Value("${updatedCriteriaStatusReportTemplate}") String template,
            UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet,
            CertificationCriteriaManager criteriaManager,
            CertificationCriterionComparator certificationCriterionComparator,
            CriteriaUpToDateReportService reportService,
            Environment env) {
        this.template = template;
        this.updatedCriteriaStatusReportSheet = updatedCriteriaStatusReportSheet;
        this.criteriaManager = criteriaManager;
        this.certificationCriterionComparator = certificationCriterionComparator;
        this.reportService = reportService;
        this.env = env;
    }

    public File generateSpreadsheet(List<Long> acbIds, Pair<LocalDate, LocalDate> requiredByDateRange) throws IOException {
        LOGGER.info("Generating Updated Criteria Charts for all active criteria");

        File newFile = copyTemplateFileToTemporaryFile(template, getFilename());
        Workbook workbook = getWorkbook(newFile);
        List<CertificationBody> acbs = acbIds.stream().map(acbId -> CertificationBody.builder().id(acbId).build()).collect(Collectors.toList());
        LOGGER.info("Getting up-to-date data for all criteria on a monthly basis for the past year");
        List<CriteriaUpToDateReport> allCriteriaUpToDateReports = reportService.getMonthlyCriteriaUpToDateReports(acbs, requiredByDateRange);
        LOGGER.info("Completed getting up-to-date data for all criteria on a monthly basis for the past year");

        criteriaManager.getActiveToday().stream()
                .sorted(certificationCriterionComparator)
                .forEach(crit ->  updatedCriteriaStatusReportSheet.generateSheetForCriteriaOnDates(crit, acbIds, allCriteriaUpToDateReports, workbook));

        //Remove the template sheet
        workbook.removeSheetAt(0);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        LOGGER.info("Completed generating Updated Criteria Charts for all active criteria");
        return writeFileToDisk(workbook, newFile);
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.fileName").toString();
    }
}
