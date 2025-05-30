package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
public class ReportDateService {
    private static final Integer MAX_DAYS_TO_CHECK_FOR_DATA = 7;

    private UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao;
    private SummaryStatisticsDAO summaryStatisticsDao;

    @Autowired
    public ReportDateService(UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao,
            SummaryStatisticsDAO summaryStatisticsDao) {
        this.updatedCriteriaStatusReportDao = updatedCriteriaStatusReportDao;
        this.summaryStatisticsDao = summaryStatisticsDao;
    }

    public LocalDate findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate preferredDate) {
        LOGGER.info("Looking for date with all necessary report data closest to " + preferredDate);

        for (Integer offset : getDayOffsetList()) {
            LocalDate possibleReportDate = preferredDate.plusDays(offset);
            boolean criterionStatusReportsExist = updatedCriteriaStatusReportDao.doUpdatedCriterionStatusReportsExistOnDay(possibleReportDate);
            StatisticsSnapshot statisticsSnapshot = summaryStatisticsDao.getSummaryStatistics(possibleReportDate);
            if (criterionStatusReportsExist && statisticsSnapshot != null
                    && !CollectionUtils.isEmpty(statisticsSnapshot.getAttestedCriterionStatistics())) {
                LOGGER.info("Using " + possibleReportDate + " as report date");
                return possibleReportDate;
            }
        }

        LOGGER.warn("No dates with both Update Criteria Reports and Summary Statistics data were found within " + MAX_DAYS_TO_CHECK_FOR_DATA + " days of today.");
        // we don't really ever expect to get to this point - there must be a date with both reports having data
        return preferredDate;
    }

    private List<Integer> getDayOffsetList() {
        //This generates a list in the pattern 0, -1, 1, -2, 2, -3, 3 ....
        List<Integer> dayOffsets = new ArrayList<Integer>();

        for (Integer i = 0; i < MAX_DAYS_TO_CHECK_FOR_DATA; i++) {
            Integer offset = i / 2;
            if (i % 2 == 1) {
                offset = offset * -1;
            }
            dayOffsets.add(offset);
        }
        return dayOffsets;
    }
}
