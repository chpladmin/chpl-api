 package gov.healthit.chpl.report.curesupdate;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CuresUpdateReportService {
    private static final Integer MONTHS_IN_YEAR = 12;

    private CuresStatisticsChartData curesStatisticsChartData;
    private Integer maxDaysToCheckForData;
    private CertificationCriterionService certificationCriterionService;

    public CuresUpdateReportService(CuresStatisticsChartData curesStatisticsChartData, CertificationCriterionService certificationCriterionService,
            @Value("${maxDaysToCheckForData}") Integer maxDaysToCheckForData) {

        this.curesStatisticsChartData = curesStatisticsChartData;
        this.certificationCriterionService = certificationCriterionService;
        this.maxDaysToCheckForData = maxDaysToCheckForData;
    }

    public List<CuresCriterionChartStatistic> getReportData() {
        return getDataOverTime(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10));
    }

    private List<CuresCriterionChartStatistic> getDataOverTime(CertificationCriterion criterion) {
        List<CuresCriterionChartStatistic> dataOverTime = new ArrayList<>();

        getTargetDatesForPastYear().stream()
                .sorted()
                .forEach(targetDate -> {
                    dataOverTime.addAll(getDataAtOrNearTargetData(targetDate, criterion));
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

    private List<CuresCriterionChartStatistic> getDataAtOrNearTargetData(LocalDate targetDate, CertificationCriterion criterion) {
        List<CuresCriterionChartStatistic> data = new ArrayList<CuresCriterionChartStatistic>();

        for (Integer offset : getDayOffsetList()) {
            data.add(curesStatisticsChartData.getCuresCriterionChartStatistics(targetDate.plusDays(offset))
                    .get(criterion));
            if (isTheDataComplete(data)) {
                LOGGER.info("{} - {} - found data on {}", criterion.getNumber(), targetDate, targetDate.plusDays(offset));
                return data;
            }
        }
        LOGGER.info("{} - {} - data was not found", criterion.getNumber(), targetDate);
        return null;
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

    private Boolean isTheDataComplete(List<CuresCriterionChartStatistic> data) {
        for (CuresCriterionChartStatistic stats : data) {
            LOGGER.info("checking data completeness for {}", stats.getReportDate());
            LOGGER.info("Existing Certification Count: {}", stats.getExistingCertificationCount() != null);
            LOGGER.info("Listing Count: {}", stats.getListingCount() != null);
            LOGGER.info("New Certification Count: {}", stats.getNewCertificationCount() != null);
            LOGGER.info("Requires Update Count: {}", stats.getRequiresUpdateCount() != null);
            if (!ObjectUtils.anyNull(stats.getExistingCertificationCount(),
                stats.getListingCount(),
                stats.getNewCertificationCount(),
                stats.getRequiresUpdateCount())) {

                return true;
            }
        }
        return false;
    }

}
