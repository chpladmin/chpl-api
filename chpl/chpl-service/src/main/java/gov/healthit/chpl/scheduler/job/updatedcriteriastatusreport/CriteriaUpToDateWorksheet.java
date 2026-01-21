package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReport;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReportService;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class CriteriaUpToDateWorksheet {
    private static final Integer REQUIRES_UPDATE_COL_IDX = 27;
    private static final Integer FULLY_UP_TO_DATE_COL_IDX = 28;

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

    private static final String CHART_WORKSHEET_NAME = "Criteria Up-To-Date Chart";

    private CriteriaUpToDateReportService reportService;
    private CertificationCriterionService criterionService;
    private CertificationBodyDAO acbDao;
    private List<CriteraToRowMap> criteriaToRowMaps = new ArrayList<CriteriaUpToDateWorksheet.CriteraToRowMap>();

    @Autowired
    public CriteriaUpToDateWorksheet(CriteriaUpToDateReportService reportService,
            CertificationCriterionService criterionService,
            CertificationBodyDAO acbDao) {
        this.reportService = reportService;
        this.criterionService = criterionService;
        this.acbDao = acbDao;

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

    public void populateWithDataForAllAcbsOnDate(List<Long> acbIds, LocalDate reportDataDate, Pair<LocalDate, LocalDate> requiredByDateRange, Workbook workbook)
            throws IOException {
        List<CertificationBody> acbs = acbDao.findAllActive().stream()
            .filter(acb -> acbIds.contains(acb.getId()))
            .collect(Collectors.toList());
        List<CriteriaUpToDateReport> reports = reportService.getAllCriteriaUpToDateReports(reportDataDate, acbs, requiredByDateRange);
        updateChartTitle(workbook.getSheetAt(0), reportDataDate);
        populateData(workbook.getSheetAt(0), reports);
    }

    public void generateSheetForAcbOnDate(Long acbId, LocalDate reportDataDate, Pair<LocalDate, LocalDate> requiredByDateRange, Workbook workbook) {
        CertificationBody acb = null;
        try {
            acb = acbDao.getById(acbId);
        } catch (Exception ex) {
            LOGGER.error("No ACB found with ID " + acbId, ex);
        }

        if (acb == null) {
            return;
        }
        LOGGER.info("Generating worksheet for ACB " + acb.getName());
        List<CriteriaUpToDateReport> reports = reportService.getAllCriteriaUpToDateReports(reportDataDate, Stream.of(acb).toList(), requiredByDateRange);
        Sheet acbWorksheet = addWorksheetForAcb(acb, workbook);
        updateChartTitle(acbWorksheet, reportDataDate);
        populateData(acbWorksheet, reports);
        LOGGER.info("Completed generating worksheet for ACB " + acb.getName());
    }

    private Sheet addWorksheetForAcb(CertificationBody acb, Workbook workbook) {
        Sheet sheet = workbook.cloneSheet(0);
        int num = workbook.getSheetIndex(sheet);
        workbook.setSheetName(num, getWorksheetName(acb));
        return sheet;
    }

    private void populateData(Sheet sheet, List<CriteriaUpToDateReport> reports) {
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
        List<CriteriaUpToDateReport> upToDateReportsForCriteriaPerAcb = reports.stream()
            .filter(report -> report.getCriterion().getId().equals(criterion.getId()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(upToDateReportsForCriteriaPerAcb)) {
            return null;
        }

        return CriteriaUpToDateReport.builder()
                .accurateAsOfDate(upToDateReportsForCriteriaPerAcb.get(0).getAccurateAsOfDate())
                .activeListingsAttestingToCriterionCount(upToDateReportsForCriteriaPerAcb.stream()
                        .map(report -> report.getActiveListingsAttestingToCriterionCount())
                        .collect(Collectors.summingLong(Long::longValue)))
                .activeListingsUpToDateOnCriterionCount(upToDateReportsForCriteriaPerAcb.stream()
                        .map(report -> report.getActiveListingsUpToDateOnCriterionCount())
                        .collect(Collectors.summingLong(Long::longValue)))
                .build();
    }

    private void updateChartTitle(Sheet sheet, LocalDate reportDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        XSSFDrawing drawing = ((XSSFSheet) sheet).getDrawingPatriarch();
        if (drawing != null) {
            List<XSSFChart> charts = drawing.getCharts();
            if (charts != null && charts.size() > 0) {
                // This goes into the XML that makes up the chart to set the data in the title.
                // This has potential to vary from chart to chart, based on formatting.
                charts.get(0).getCTChart().getTitle().getTx().getRich().getPArray(1).getRArray(0).setT(reportDate.format(formatter));
            }
        }
    }

    private String getWorksheetName(CertificationBody acb) {
        if (acb != null) {
            return acb.getName();
        }
        return CHART_WORKSHEET_NAME;
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
