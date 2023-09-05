package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.time.Period;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class CuresStatisticsCreatorJob  extends QuartzJob {
    private static final int ONE_DAY = 1;

    @Autowired
    private OriginalCriterionActivityStatisticsCalculator originalCriterionActivityStatisticsCalculator;

    @Autowired
    private CuresCriterionActivityStatisticsCalculator curesCriterionActivityStatisticsCalculator;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        originalCriterionActivityStatisticsCalculator.setOriginalCriterionActivityStatisticsForDate(yesterday);
        curesCriterionActivityStatisticsCalculator.setCuresCriterionActivityStatisticsForDate(yesterday);

        LOGGER.info("*****Cures Reporting Statistics Job is complete.*****");
    }
}
