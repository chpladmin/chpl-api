package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.CuresStatisticsChartData;

@Component
public class CuresChartsOverTimeSheet {

    private CuresStatisticsChartData curesStatisticsChartData;

    @Autowired
    public CuresChartsOverTimeSheet(CuresStatisticsChartData curesStatisticsChartData) {
        this.curesStatisticsChartData = curesStatisticsChartData;
    }

    public void populate(Sheet sheet, CertificationCriterionDTO criterion) {
        Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> dataOverTime = getDataOverTime();
        Integer columnIndex = 1;

        for (LocalDate dateForColumn : dataOverTime.keySet()) {
            populateColumn(dataOverTime.get(dateForColumn).get(criterion),
                dateForColumn,
                sheet,
                columnIndex);

            columnIndex++;
        }
    }

    private void populateColumn(CuresCriterionChartStatistic stats, LocalDate dateForColumn, Sheet sheet, Integer columnIndex) {
        Row currentRow = sheet.getRow(0);
        currentRow.getCell(columnIndex).setCellValue(dateForColumn);

        currentRow = sheet.getRow(1);
        currentRow.getCell(columnIndex).setCellValue(stats.getRequiresUpdateCount());

        currentRow = sheet.getRow(2);
        currentRow.getCell(columnIndex).setCellValue(stats.getExistingCertificationCount());

        currentRow = sheet.getRow(3);
        currentRow.getCell(columnIndex).setCellValue(stats.getNewCertificationCount());
    }

    private Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> getDataOverTime() {
        Map<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>> dataOverTime =
                new TreeMap<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>>();

        getTargetDatesForPastYear().stream()
                .sorted()
                .forEach(targetDate -> dataOverTime.put(targetDate, curesStatisticsChartData.getCuresCriterionChartStatistics(targetDate)));

        return dataOverTime;
    }

    private List<LocalDate> getTargetDatesForPastYear() {
        List<LocalDate> targetDates = new ArrayList<LocalDate>();
        for (Integer i = 0; i <= 10; ++i) {
            targetDates.add(LocalDate.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()));
        }
        return targetDates;
    }
}
