package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.CuresStatisticsChartData;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresChartsOverTimeSheet {
    private static final Integer DATE_ROW_IDX = 0;
    private static final Integer REQUIRES_UPDATE_ROW_IDX = 1;
    private static final Integer EXISTING_CERTIFICATION_ROW_IDX = 2;
    private static final Integer NEW_CERTIFICATIONS_ROW_IDX = 3;
    private static final Integer MONTHS_IN_YEAR = 12;

    private CuresStatisticsChartData curesStatisticsChartData;
    private Integer maxDaysToCheckForData;

    @Autowired
    public CuresChartsOverTimeSheet(CuresStatisticsChartData curesStatisticsChartData, @Value("${maxDaysToCheckForData}") Integer maxDaysToCheckForData) {
        this.curesStatisticsChartData = curesStatisticsChartData;
        this.maxDaysToCheckForData = maxDaysToCheckForData;
    }

    public void populate(Sheet sheet, CertificationCriterion criterion) {
        Map<LocalDate, Map<CertificationCriterion, CuresCriterionChartStatistic>> dataOverTime = getDataOverTime(criterion);
        Integer columnIndex = 1;

        for (LocalDate dateForColumn : dataOverTime.keySet()) {
            CuresCriterionChartStatistic stat = dataOverTime.containsKey(dateForColumn)
                        && dataOverTime.get(dateForColumn) != null
                        && dataOverTime.get(dateForColumn).containsKey(criterion)
                        && dataOverTime.get(dateForColumn).get(criterion) != null
                    ? dataOverTime.get(dateForColumn).get(criterion)
                    : null;

            populateColumn(stat, dateForColumn, sheet, columnIndex);

            columnIndex++;
        }
    }

    private void populateColumn(CuresCriterionChartStatistic stats, LocalDate dateForColumn, Sheet sheet, Integer columnIndex) {
        Row currentRow = sheet.getRow(DATE_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(dateForColumn.minusDays(1)); //Last day of the previous month

        currentRow = sheet.getRow(REQUIRES_UPDATE_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getRequiresUpdateCount());

        currentRow = sheet.getRow(EXISTING_CERTIFICATION_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getExistingCertificationCount());

        currentRow = sheet.getRow(NEW_CERTIFICATIONS_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getNewCertificationCount());
    }

    private Map<LocalDate, Map<CertificationCriterion, CuresCriterionChartStatistic>> getDataOverTime(CertificationCriterion criterion) {
        Map<LocalDate, Map<CertificationCriterion, CuresCriterionChartStatistic>> dataOverTime =
                new TreeMap<LocalDate, Map<CertificationCriterion, CuresCriterionChartStatistic>>();

        getTargetDatesForPastYear().stream()
                .sorted()
                .forEach(targetDate -> {
                    Map<CertificationCriterion, CuresCriterionChartStatistic> stats = getDataAtOrNearTargetData(targetDate, criterion);
                    dataOverTime.put(targetDate, stats);
                });

        return dataOverTime;
    }

    private List<LocalDate> getTargetDatesForPastYear() {
        List<LocalDate> targetDates = new ArrayList<LocalDate>();
        for (Integer i = 0; i < MONTHS_IN_YEAR; ++i) {
            targetDates.add(LocalDate.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()));
        }
        return targetDates;
    }

    private Map<CertificationCriterion, CuresCriterionChartStatistic> getDataAtOrNearTargetData(LocalDate targetDate, CertificationCriterion criterion) {
        Map<CertificationCriterion, CuresCriterionChartStatistic> data = null;

        for (Integer offset : getDayOffsetList()) {
            data = curesStatisticsChartData.getCuresCriterionChartStatistics(targetDate.plusDays(offset));
            if (isTheDataComplete(data)) {
                LOGGER.info("{} - {} - found data on {}", criterion.getNumber(), targetDate, targetDate.plusDays(offset));
                return data;
            }
        }
        LOGGER.info("{} - {} - data was not found", criterion.getNumber(), targetDate);
        return null;
    }

    private Boolean isTheDataComplete(Map<CertificationCriterion, CuresCriterionChartStatistic> data) {
        for (CuresCriterionChartStatistic stats : data.values()) {
            if (ObjectUtils.anyNull(stats.getExistingCertificationCount(),
                stats.getListingCount(),
                stats.getNewCertificationCount(),
                stats.getRequiresUpdateCount())) {

                return false;
            }
        }
        return true;
    }

    private List<Integer> getDayOffsetList() {
        //This generates a list in the pattern 0, -1, 1, -2, 2, -3, 3 ....
        List<Integer> dayOffsets = new ArrayList<Integer>();

        for (Integer i = 0; i < maxDaysToCheckForData; i++) {
            Integer offset = i / 2;
            if (i % 2 == 1) {
                offset = offset * -1;
            }
            dayOffsets.add(offset);
        }
        return dayOffsets;
    }
}
