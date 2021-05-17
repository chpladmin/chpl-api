package gov.healthit.chpl.scheduler.job.curesReporting;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dto.statistics.CriterionListingCountStatisticDTO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesReportingJobLogger")
public class CuresReportingStatisticsJob  extends QuartzJob {
    private static final int ONE_DAY = 1;

    @Autowired
    private CriterionListingStatisticsCalculator criterionListingStatisticsCalculator;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        setCriterionListingCountStatisticsForDate(yesterday);
        //TODO: other statistic types

        LOGGER.info("*****Cures Reporting Statistics Job is complete.*****");
    }

    private void setCriterionListingCountStatisticsForDate(LocalDate statisticDate) {
        //TODO: do as one transaction
        if (criterionListingStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
            criterionListingStatisticsCalculator.deleteStatisticsForDate(statisticDate);
        }
        List<CriterionListingCountStatisticDTO> currentStatistics = criterionListingStatisticsCalculator.calculateCurrentStatistics(statisticDate);
        criterionListingStatisticsCalculator.save(currentStatistics);
    }
}
