package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
                new HashMap<LocalDate, Map<CertificationCriterionDTO, CuresCriterionChartStatistic>>();

        LocalDate d1 = LocalDate.of(2021, 6, 2);
        LocalDate d2 = LocalDate.of(2021, 7, 1);
        LocalDate d3 = LocalDate.of(2021, 8, 1);
        LocalDate d4 = LocalDate.of(2021, 9, 1);
        LocalDate d5 = LocalDate.of(2021, 10, 1);
        LocalDate d6 = LocalDate.of(2021, 11, 1);
        LocalDate d7 = LocalDate.of(2021, 12, 1);
        LocalDate d8 = LocalDate.of(2022, 1, 1);
        LocalDate d9 = LocalDate.of(2022, 2, 1);
        LocalDate d10 = LocalDate.of(2022, 3, 1);
        LocalDate d11 = LocalDate.of(2022, 4, 1);

        dataOverTime.put(d1, curesStatisticsChartData.getCuresCriterionChartStatistics(d1));
        dataOverTime.put(d2, curesStatisticsChartData.getCuresCriterionChartStatistics(d2));
        dataOverTime.put(d3, curesStatisticsChartData.getCuresCriterionChartStatistics(d3));
        dataOverTime.put(d4, curesStatisticsChartData.getCuresCriterionChartStatistics(d4));
        dataOverTime.put(d5, curesStatisticsChartData.getCuresCriterionChartStatistics(d5));
        dataOverTime.put(d6, curesStatisticsChartData.getCuresCriterionChartStatistics(d6));
        dataOverTime.put(d7, curesStatisticsChartData.getCuresCriterionChartStatistics(d7));
        dataOverTime.put(d8, curesStatisticsChartData.getCuresCriterionChartStatistics(d8));
        dataOverTime.put(d9, curesStatisticsChartData.getCuresCriterionChartStatistics(d9));
        dataOverTime.put(d10, curesStatisticsChartData.getCuresCriterionChartStatistics(d10));
        dataOverTime.put(d11, curesStatisticsChartData.getCuresCriterionChartStatistics(d11));

        return dataOverTime;
    }
}
