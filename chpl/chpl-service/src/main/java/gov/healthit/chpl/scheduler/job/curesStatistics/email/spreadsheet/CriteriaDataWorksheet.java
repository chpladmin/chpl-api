package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.CuresStatisticsChartData;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CriteriaDataWorksheet {
    private static final Integer EXISTING_CERTIFICATION_COL_IDX = 1;
    private static final Integer NEW_CERTIFICATION_COL_IDX = 2;
    private static final Integer REQUIRES_UPDATE_COL_IDX = 3;
    private static final Integer LISTING_COUNT_COL_IDX = 4;

    private static final Integer B_1_CURES_ROW_IDX = 1;
    private static final Integer B_2_CURES_ROW_IDX = 2;
    private static final Integer B_3_CURES_ROW_IDX = 3;
    private static final Integer B_7_CURES_ROW_IDX = 4;
    private static final Integer B_8_CURES_ROW_IDX = 5;
    private static final Integer B_9_CURES_ROW_IDX = 6;
    private static final Integer B_10_ROW_IDX = 7;
    private static final Integer C_3_CURES_ROW_IDX = 8;
    private static final Integer D_2_CURES_ROW_IDX = 9;
    private static final Integer D_3_CURES_ROW_IDX = 10;
    private static final Integer D_10_CURES_ROW_IDX = 11;
    private static final Integer D_12_ROW_IDX = 12;
    private static final Integer D_13_ROW_IDX = 13;
    private static final Integer E_1_CURES_ROW_IDX = 14;
    private static final Integer F_5_CURES_ROW_IDX = 15;
    private static final Integer G_6_CURES_ROW_IDX = 16;
    private static final Integer G_9_CURES_ROW_IDX = 17;
    private static final Integer G_10_ROW_IDX = 18;

    private CertificationCriterionService criterionService;
    private CuresStatisticsChartData curesStatisticsChartData;

    private List<CriteraToRowMap> criteriaToRowMaps = new ArrayList<CriteraToRowMap>();


    @Autowired
    public CriteriaDataWorksheet(CertificationCriterionService criterionService, CuresStatisticsChartData curesStatisticsChartData) {
        this.criterionService = criterionService;
        this.curesStatisticsChartData = curesStatisticsChartData;

        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_1_CURES, B_1_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_2_CURES, B_2_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_3_CURES, B_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_7_CURES, B_7_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_8_CURES, B_8_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_9_CURES, B_9_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_10, B_10_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.C_3_CURES, C_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_2_CURES, D_2_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_3_CURES, D_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_10_CURES, D_10_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_12, D_12_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_13, D_13_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.E_1_CURES, E_1_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_5_CURES, F_5_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_6_CURES, G_6_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_9_CURES, G_9_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_10, G_10_ROW_IDX));
    }

    public Workbook populate(Workbook workbook) {
        LOGGER.info("Populating sheet Criteria Data");
        Sheet sheet = workbook.getSheet("Criteria Data");

        LocalDate reportDate = curesStatisticsChartData.getReportDate();
        Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap = curesStatisticsChartData.getCuresCriterionChartStatistics(reportDate);

        criteriaToRowMaps.stream()
                .forEach(map ->
                        writeDataForCuresCriterionChartStatistic(
                                getCuresCriterionChartStatisticByCriterion(
                                        dataMap, criterionService.get(map.getCriteriaKey())), sheet.getRow(map.getRowNumber())));

        return workbook;
    }

    private CuresCriterionChartStatistic getCuresCriterionChartStatisticByCriterion(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap, CertificationCriterion criterion) {
        return dataMap.entrySet().stream()
                .filter(entry -> entry.getKey().getId().equals(criterion.getId()))
                .findFirst()
                .get()
                .getValue();
    }

    private void writeDataForCuresCriterionChartStatistic(CuresCriterionChartStatistic data, Row row) {
        row.getCell(EXISTING_CERTIFICATION_COL_IDX).setCellValue(data.getExistingCertificationCount());
        row.getCell(NEW_CERTIFICATION_COL_IDX).setCellValue(data.getNewCertificationCount());
        row.getCell(REQUIRES_UPDATE_COL_IDX).setCellValue(data.getRequiresUpdateCount());
        row.getCell(LISTING_COUNT_COL_IDX).setCellValue(data.getListingCount());
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
