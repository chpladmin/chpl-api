package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet.CuresSpreadsheet;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class UpdatedCriteriaStatusReportWorkbook extends CuresSpreadsheet { //TODO - need to update the name of the class that is being extended
    private UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet;
    private UpdatedCriteriaStatusReportDAO updatedCriteriaStatusReportDAO;
    private String template;
    private CertificationCriterionService certificationCriterionService;
    private CertificationCriterionComparator certificationCriterionComparator;

    public UpdatedCriteriaStatusReportWorkbook(@Value("${updatedCriteriaStatusReportTemplate}") String template,
            UpdatedCriteriaStatusReportSheet updatedCriteriaStatusReportSheet,
            UpdatedCriteriaStatusReportDAO updatedCriteriaStatusReportDAO,
            CertificationCriterionService certificationCriterionService,
            CertificationCriterionComparator certificationCriterionComparator) {
        this.template = template;
        this.updatedCriteriaStatusReportSheet = updatedCriteriaStatusReportSheet;
        this.updatedCriteriaStatusReportDAO = updatedCriteriaStatusReportDAO;
        this.certificationCriterionService = certificationCriterionService;
        this.certificationCriterionComparator = certificationCriterionComparator;
    }

    public File generateSpreadsheet() throws IOException {
        File newFile = copyTemplateFileToTemporaryFile(template, "CuresChartsOverTime");
        Workbook workbook = getWorkbook(newFile);

        updatedCriteriaStatusReportDAO.getCriteriaIdsFromUpdatedCritieriaStatusReport().stream()
                .map(id -> certificationCriterionService.get(id))
                .sorted(certificationCriterionComparator)
                .forEach(crit ->  updatedCriteriaStatusReportSheet.generateSheetForCriteria(crit, workbook));

        //Remove the template sheet
        workbook.removeSheetAt(0);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }

}
