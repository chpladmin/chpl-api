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
    private CriterionListingStatisticsCalculator criterionListingStatisticsCalculator;

    @Autowired
    private OriginalCriterionActivityStatisticsCalculator originalCriterionActivityStatisticsCalculator;

    @Autowired
    private CuresCriterionActivityStatisticsCalculator curesCriterionActivityStatisticsCalculator;

    @Autowired
    private ListingCuresStatusStatisticsCalculator listingCuresStatusStatisticsCalculator;

    @Autowired
    private PrivacyAndSecurityListingStatisticsCalculator privacyAndSecurityListingStatisticsCalculator;

    @Autowired
    private ListingCriterionForCuresAchievementStatisticsCalculator listingCriterionForCuresAchievementStatisticsCalculator;

    @Autowired
    private CuresCriteriaStatisticsByAcbCalculator curesCrieriaStatisticsByAcbCalculator;

    @Autowired
    private CuresListingByAcbStatisticsCalculator curesListingStatisticsCalculator;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        criterionListingStatisticsCalculator.setCriterionListingCountStatisticsForDate(yesterday);
        originalCriterionActivityStatisticsCalculator.setOriginalCriterionActivityStatisticsForDate(yesterday);
        curesCriterionActivityStatisticsCalculator.setCuresCriterionActivityStatisticsForDate(yesterday);
        listingCuresStatusStatisticsCalculator.setListingCuresStatusStatisticsForDate(yesterday);
        privacyAndSecurityListingStatisticsCalculator.setPrivacyAndSecurityListingStatisticsForDate(yesterday);
        listingCriterionForCuresAchievementStatisticsCalculator.setCriteriaNeededToAchieveCuresStatisticsForDate(yesterday);
        curesCrieriaStatisticsByAcbCalculator.setCuresStatisticsByAcbForDate(yesterday);
        curesListingStatisticsCalculator.setCuresListingStatisticsForDate(yesterday);

        LOGGER.info("*****Cures Reporting Statistics Job is complete.*****");
    }
}
