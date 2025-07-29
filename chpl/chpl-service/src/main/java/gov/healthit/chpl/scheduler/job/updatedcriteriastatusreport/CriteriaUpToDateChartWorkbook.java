package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateStatusReportDateService;

@Component
public class CriteriaUpToDateChartWorkbook extends UpdatedCriteriaSpreadsheetBase {
    private CriteriaUpToDateWorksheet criteriaUpToDateWorksheet;
    private String template;
    private CriteriaUpToDateStatusReportDateService reportDateService;
    private Environment env;

    public CriteriaUpToDateChartWorkbook(@Value("${criteriaUpToDateChartSpreadsheetTemplate}") String template,
            CriteriaUpToDateWorksheet criteriaUpToDateWorksheet,
            CriteriaUpToDateStatusReportDateService reportDateService,
            Environment env) {
        this.template = template;
        this.criteriaUpToDateWorksheet = criteriaUpToDateWorksheet;
        this.reportDateService = reportDateService;
        this.env = env;
    }

    public File generateSpreadsheet() throws IOException {
        File newFile = copyTemplateFileToTemporaryFile(template, getFilename());
        Workbook workbook = getWorkbook(newFile);
        LocalDate reportDate = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now());

        criteriaUpToDateWorksheet.populateWithDataOnDate(reportDate, workbook);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        return writeFileToDisk(workbook, newFile);
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.chart.filename").toString();
    }
}
