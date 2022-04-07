package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class CuresChartsOverTimeSpreadheet {

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
        File newFile = copyTemplateFileToTemporaryFile();
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

    private File copyTemplateFileToTemporaryFile() throws IOException {
        try (InputStream srcInputStream = getTemplateAsStream()) {
            File tempFile = File.createTempFile("CuresChartsOverTime_", ".xlsx");
            Files.copy(srcInputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    private InputStream getTemplateAsStream() {
        return getClass().getClassLoader().getResourceAsStream(template);
    }

    private Workbook getWorkbook(File newFile) throws IOException {
        FileInputStream fis = new FileInputStream(newFile);
        return new XSSFWorkbook(fis);
    }

    private File writeFileToDisk(Workbook workbook, File saveFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
            workbook.write(outputStream);
            workbook.close();
        }
        return saveFile;
    }

}

