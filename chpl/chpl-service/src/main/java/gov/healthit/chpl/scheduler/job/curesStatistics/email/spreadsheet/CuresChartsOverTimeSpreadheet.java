package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public final class CuresChartsOverTimeSpreadheet extends CuresSpreadsheet {

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

        curesChartsOverTimeSheet.populate(workbook.getSheet("(b)(10)"),
                certificationCriteriaService.get(CertificationCriterionService.Criteria2015.B_10));

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }
}

