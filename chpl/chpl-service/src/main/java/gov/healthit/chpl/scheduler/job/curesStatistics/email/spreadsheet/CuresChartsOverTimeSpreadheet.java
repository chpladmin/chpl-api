package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class CuresChartsOverTimeSpreadheet extends CuresSpreadsheet {

    private String template;
    private CertificationCriterionService certificationCriteriaService;
    private CuresChartsOverTimeSheet curesChartsOverTimeSheet;

    @Autowired
    public CuresChartsOverTimeSpreadheet(CertificationCriterionService certificationCriteriaService,
            CuresChartsOverTimeSheet curesChartsOverTimeSheet,
            @Value("${curesChartsOverTimeSpreadsheetTemplate}") String template) {

        this.certificationCriteriaService = certificationCriteriaService;
        this.curesChartsOverTimeSheet = curesChartsOverTimeSheet;
        this.template = template;
    }

    public File generateSpreadsheet() throws IOException {
        File newFile = copyTemplateFileToTemporaryFile(template, "CuresChartsOverTime");
        Workbook workbook = getWorkbook(newFile);

        curesChartsOverTimeSheet.populate(workbook.getSheet("(b)(1)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.B_1_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(b)(2)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.B_2_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(e)(1)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.E_1_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(f)(5)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.F_5_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(g)(6)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.G_6_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(g)(9)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.G_9_CURES)));

        curesChartsOverTimeSheet.populate(workbook.getSheet("(g)(10)"),
                new CertificationCriterionDTO(certificationCriteriaService.get(CertificationCriterionService.Criteria2015.G_10)));


        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }
}

