package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;

@Component
public class UpdatedCriteriaStatusReportWorkbook extends UpdatedCriteriaSpreadsheetBase {
    private UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet;
    private String template;
    private CertificationCriteriaManager criteriaManager;
    private CertificationCriterionComparator certificationCriterionComparator;
    private Environment env;

    public UpdatedCriteriaStatusReportWorkbook(@Value("${updatedCriteriaStatusReportTemplate}") String template,
            UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet,
            CertificationCriteriaManager criteriaManager,
            CertificationCriterionComparator certificationCriterionComparator,
            Environment env) {
        this.template = template;
        this.updatedCriteriaStatusReportSheet = updatedCriteriaStatusReportSheet;
        this.criteriaManager = criteriaManager;
        this.certificationCriterionComparator = certificationCriterionComparator;
        this.env = env;
    }

    public File generateSpreadsheet() throws IOException {
        File newFile = copyTemplateFileToTemporaryFile(template, getFilename());
        Workbook workbook = getWorkbook(newFile);

        criteriaManager.getActiveToday().stream()
                .sorted(certificationCriterionComparator)
                .forEach(crit ->  updatedCriteriaStatusReportSheet.generateSheetForCriteria(crit, workbook));

        //Remove the template sheet
        workbook.removeSheetAt(0);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.fileName").toString();
    }
}
