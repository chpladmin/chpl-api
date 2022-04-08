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
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.CuresStatisticsChartData;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CuresChartsOverTimeSheet {
    private static final Integer DATE_ROW_IDX = 0;
    private static final Integer REQUIRES_UPDATE_ROW_IDX = 1;
    private static final Integer EXISTING_CERTIFICATION_ROW_IDX = 2;
    private static final Integer NEW_CERTIFICATIONS_ROW_IDX = 3;
    private static final Integer MONTHS_IN_YEAR = 12;

    private CuresStatisticsChartData curesStatisticsChartData;

    @Autowired
    public CuresChartsOverTimeSheet(CuresStatisticsChartData curesStatisticsChartData) {
        this.curesStatisticsChartData = curesStatisticsChartData;
    }

    public void populate(Sheet sheet, CertificationCriterionDTO criterion) {
        Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> dataOverTime = getDataOverTime();
        Integer columnIndex = 1;

        for (LocalDate dateForColumn : dataOverTime.keySet()) {
            CuresCriterionChartStatistic stat = dataOverTime.containsKey(dateForColumn) && dataOverTime.get(dateForColumn).containsKey(criterion)
                    ? dataOverTime.get(dateForColumn).get(criterion)
                    : null;

            populateColumn(stat, dateForColumn, sheet, columnIndex);

            columnIndex++;
        }
    }

    private void populateColumn(CuresCriterionChartStatistic stats, LocalDate dateForColumn, Sheet sheet, Integer columnIndex) {
        Row currentRow = sheet.getRow(DATE_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(dateForColumn);

        currentRow = sheet.getRow(REQUIRES_UPDATE_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getRequiresUpdateCount());

        currentRow = sheet.getRow(EXISTING_CERTIFICATION_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getExistingCertificationCount());

        currentRow = sheet.getRow(NEW_CERTIFICATIONS_ROW_IDX);
        currentRow.getCell(columnIndex).setCellValue(stats == null ? 0 : stats.getNewCertificationCount());
    }

    private Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> getDataOverTime() {
        Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> dataOverTime =
                new TreeMap<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>>();

        getTargetDatesForPastYear().stream()
                .sorted()
                .forEach(targetDate -> {
                    Map<CertificationCriterionDTO, CuresCriterionChartStatistic> stats = getDataAtOrNearTargetData(targetDate);
                    if (stats != null) {
                        dataOverTime.put(targetDate, stats);
                    }
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

    private Map<CertificationCriterionDTO, CuresCriterionChartStatistic> getDataAtOrNearTargetData(LocalDate targetDate) {
        Map<CertificationCriterionDTO, CuresCriterionChartStatistic> data = null;

        for (Integer offset : getDayOffsetList()) {
            data = curesStatisticsChartData.getCuresCriterionChartStatistics(targetDate.plusDays(offset));
            if (isTheDataComplete(data)) {
                LOGGER.info("{} - found data for {}", targetDate, targetDate.plusDays(offset));
                return data;
            }
        }
        LOGGER.info("{} - data was not found", targetDate);
        return null;
    }

    private Boolean isTheDataComplete(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> data) {
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
        //This generates a list in this pattern 0, -1, 1, -2, 2, -3, 3 ....
        List<Integer> dayOffsets = new ArrayList<Integer>();
        Integer maxDaysToCheck = 7;

        for (Integer i = 0; i < maxDaysToCheck; i++) {
            Integer offset = i / 2;
            if (i % 2 == 1) {
                offset = offset * -1;
            }
            dayOffsets.add(offset);
        }
        return dayOffsets;
    }
}
