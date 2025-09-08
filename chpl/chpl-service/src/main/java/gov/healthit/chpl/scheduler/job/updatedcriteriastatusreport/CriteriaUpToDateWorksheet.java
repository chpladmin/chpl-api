package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReport;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReportService;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class CriteriaUpToDateWorksheet {
    private static final Integer REQUIRES_UPDATE_COL_IDX = 1;
    private static final Integer FULLY_UP_TO_DATE_COL_IDX = 2;

    private static final Integer A_5_ROW_IDX = 1;
    private static final Integer A_12_ROW_IDX = 2;
    private static final Integer A_15_ROW_IDX = 3;
    private static final Integer B_1_ROW_IDX = 4;
    private static final Integer B_2_ROW_IDX = 5;
    private static final Integer B_9_ROW_IDX = 6;
    private static final Integer C_4_ROW_IDX = 7;
    private static final Integer E_1_ROW_IDX = 8;
    private static final Integer F_1_ROW_IDX = 9;
    private static final Integer F_3_ROW_IDX = 10;
    private static final Integer F_4_ROW_IDX = 11;
    private static final Integer F_5_ROW_IDX = 12;
    private static final Integer G_6_ROW_IDX = 13;
    private static final Integer G_9_ROW_IDX = 14;
    private static final Integer G_10_ROW_IDX = 15;

    private static final String DATA_WORKSHEET_NAME = "Data";
    private static final String CHART_WORKSHEET_NAME = "Criteria Up-To-Date Chart";

    private CriteriaUpToDateReportService reportService;
    private CertificationCriterionService criterionService;
    private List<CriteraToRowMap> criteriaToRowMaps = new ArrayList<CriteriaUpToDateWorksheet.CriteraToRowMap>();

    @Autowired
    public CriteriaUpToDateWorksheet(CriteriaUpToDateReportService reportService,
            CertificationCriterionService criterionService) {
        this.reportService = reportService;
        this.criterionService = criterionService;

        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.A_5, A_5_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.A_12, A_12_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.A_15, A_15_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_1_CURES, B_1_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_2_CURES, B_2_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_9_CURES, B_9_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.C_4, C_4_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.E_1_CURES, E_1_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_1, F_1_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_3, F_3_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_4, F_4_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_5_CURES, F_5_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_6_CURES, G_6_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_9_CURES, G_9_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_10, G_10_ROW_IDX));
    }

    public void populateWithDataOnDate(LocalDate reportDataDate, Workbook workbook) throws IOException {
        List<CriteriaUpToDateReport> reports = reportService.getAllCriteriaUpToDateReports(reportDataDate);
        populateDataSheet(reports, workbook);
        updateChartTitles(workbook, reportDataDate);
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    }

    private void updateChartTitles(Workbook workbook, LocalDate reportDate) {
        Sheet chartSheet = getChartSheet(workbook);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        XSSFDrawing drawing = (XSSFDrawing) chartSheet.createDrawingPatriarch();
        for (XSSFChart chart : drawing.getCharts()) {
            // This goes into the XML that makes up the chart to set the data in the title.  This has potential
            // to vary from chart to chart, based on formatting.
            chart.getCTChart().getTitle().getTx().getRich().getPArray(1).getRArray(0).setT(reportDate.format(formatter));
        }
    }

    private void populateDataSheet(List<CriteriaUpToDateReport> reports, Workbook workbook) {
        Sheet sheet = getDataSheet(workbook);

        criteriaToRowMaps.stream()
                .forEach(map ->
                writeDataForCriterionUpToDateChartStatistic(
                        getUpToDateReportByCriterion(
                                reports, criterionService.get(map.getCriteriaKey())), sheet.getRow(map.getRowNumber())));
    }

    private void writeDataForCriterionUpToDateChartStatistic(CriteriaUpToDateReport data, Row row) {
        row.getCell(FULLY_UP_TO_DATE_COL_IDX).setCellValue(data.getActiveListingsUpToDateOnCriterionCount());
        row.getCell(REQUIRES_UPDATE_COL_IDX).setCellValue(data.getActiveListingsAttestingToCriterionCount() - data.getActiveListingsUpToDateOnCriterionCount());
    }

    private CriteriaUpToDateReport getUpToDateReportByCriterion(List<CriteriaUpToDateReport> reports, CertificationCriterion criterion) {
        return reports.stream()
            .filter(report -> report.getCriterion().getId().equals(criterion.getId()))
            .findAny()
            .get();
    }

    private Sheet getDataSheet(Workbook workbook) {
        return workbook.getSheet(DATA_WORKSHEET_NAME);
    }

    private Sheet getChartSheet(Workbook workbook) {
        return workbook.getSheet(CHART_WORKSHEET_NAME);
    }

    class CriteraToRowMap {
        private String criteriaKey;
        private Integer rowNumber;

        CriteraToRowMap(String criteriaKey, Integer rowNumber) {
            this.criteriaKey = criteriaKey;
            this.rowNumber = rowNumber;
        }

        public String getCriteriaKey() {
            return criteriaKey;
        }

        public void setCriteriaKey(String criteriaKey) {
            this.criteriaKey = criteriaKey;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }
    }
}
