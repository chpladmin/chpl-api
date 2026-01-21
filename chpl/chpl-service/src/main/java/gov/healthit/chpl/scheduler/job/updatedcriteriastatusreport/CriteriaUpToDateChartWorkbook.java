package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateStatusReportDateService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
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

    public File generateSpreadsheet(List<Long> acbIds, Pair<LocalDate, LocalDate> requiredByDateRange) throws IOException {
        LOGGER.info("Generating Criteria Up-To-Date Workbook");
        File newFile = copyTemplateFileToTemporaryFile(template, getFilename());
        Workbook workbook = getWorkbook(newFile);
        LocalDate reportDate = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now());

        LOGGER.info("Populating worksheet for all ACBs: " + acbIds);
        criteriaUpToDateWorksheet.populateWithDataForAllAcbsOnDate(acbIds, reportDate, requiredByDateRange, workbook);
        if (acbIds.size() > 1) {
            acbIds.stream()
                .forEach(acbId -> criteriaUpToDateWorksheet.generateSheetForAcbOnDate(acbId, reportDate, requiredByDateRange, workbook));
        }

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        LOGGER.info("Completed generating Criteria Up-To-Date Workbook");
        return writeFileToDisk(workbook, newFile);
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.chart.filename").toString();
    }
}
