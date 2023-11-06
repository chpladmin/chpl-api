package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.time.Period;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.curesStatistics.CriterionListingStatisticsCalculator;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class BackfillMissingCuresStatisticsDataJob extends QuartzJob {
    private static final int ONE_DAY = 1;

    @Autowired
    private CriterionListingStatisticsCalculator cureCriterionListingStatisticsCalculator;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Backfiling Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        LocalDate startDayOfMissingData = LocalDate.of(2023, 9, 5);
        while (startDayOfMissingData.isBefore(yesterday)) {
            if (!cureCriterionListingStatisticsCalculator.hasStatisticsForDate(startDayOfMissingData)) {
                cureCriterionListingStatisticsCalculator.setCriterionListingCountStatisticsForDate(startDayOfMissingData);
            }
            startDayOfMissingData = startDayOfMissingData.plusDays(ONE_DAY);
        }

        LOGGER.info("*****Backfilling Cures Reporting Statistics Job is complete.*****");
    }

}
