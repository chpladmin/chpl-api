package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.statistics.CriterionListingCountStatistic;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
import gov.healthit.chpl.domain.statistics.CuresCriterionUpgradedWithoutOriginalListingStatistic;
import gov.healthit.chpl.domain.statistics.CuresListingStatistic;
import gov.healthit.chpl.domain.statistics.CuresStatisticsByAcb;
import gov.healthit.chpl.domain.statistics.ListingCuresStatusStatistic;
import gov.healthit.chpl.domain.statistics.ListingToCriterionForCuresAchievementStatistic;
import gov.healthit.chpl.domain.statistics.PrivacyAndSecurityListingStatistic;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class CuresStatisticsCreatorJob  extends QuartzJob {
    private static final int ONE_DAY = 1;

    @Autowired
    private JpaTransactionManager txManager;

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
    private CuresCrieriaUpdateByAcbCalculator curesCrieriaUpdateByAcbCalculator;

    @Autowired
    private CuresListingStatisticsCalculator curesListingStatisticsCalculator;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        //setCriterionListingCountStatisticsForDate(yesterday);
        //setOriginalCriterionActivityStatisticsForDate(yesterday);
        //setCuresCriterionActivityStatisticsForDate(yesterday);
        //setListingCuresStatusStatisticsForDate(yesterday);
        //setPrivacyAndSecurityListingStatisticsForDate(yesterday);
        //setCriteriaNeededToAchieveCuresStatisticsForDate(yesterday);
        //setCuresStatisticsByAcbForDate(yesterday);
        setCuresListingStatisticsForDate(yesterday);

        LOGGER.info("*****Cures Reporting Statistics Job is complete.*****");
    }

    private void setCuresListingStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (curesListingStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    curesListingStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<CuresListingStatistic> stats = curesListingStatisticsCalculator.calculate(statisticDate);
                curesListingStatisticsCalculator.save(stats);
            }

        });
    }

    private void setCuresStatisticsByAcbForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (curesCrieriaUpdateByAcbCalculator.hasStatisticsForDate(statisticDate)) {
                    curesCrieriaUpdateByAcbCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<CuresStatisticsByAcb> stats = curesCrieriaUpdateByAcbCalculator.calculate(statisticDate);
                curesCrieriaUpdateByAcbCalculator.save(stats);
            }

        });
    }

    private void setCriterionListingCountStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (criterionListingStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    criterionListingStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<CriterionListingCountStatistic> currentStatistics = criterionListingStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                criterionListingStatisticsCalculator.save(currentStatistics);
            }
        });
    }

    private void setOriginalCriterionActivityStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (originalCriterionActivityStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    originalCriterionActivityStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<CriterionUpgradedToCuresFromOriginalListingStatistic> currentStatistics = originalCriterionActivityStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                originalCriterionActivityStatisticsCalculator.save(currentStatistics);
            }
        });
    }

    private void setCuresCriterionActivityStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (curesCriterionActivityStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    curesCriterionActivityStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<CuresCriterionUpgradedWithoutOriginalListingStatistic> currentStatistics = curesCriterionActivityStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                curesCriterionActivityStatisticsCalculator.save(currentStatistics);
            }
        });
    }

    private void setListingCuresStatusStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (listingCuresStatusStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    listingCuresStatusStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                ListingCuresStatusStatistic currentStatistic = listingCuresStatusStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                listingCuresStatusStatisticsCalculator.save(currentStatistic);
            }
        });
    }

    private void setPrivacyAndSecurityListingStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (privacyAndSecurityListingStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    privacyAndSecurityListingStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                PrivacyAndSecurityListingStatistic currentStatistic = privacyAndSecurityListingStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                privacyAndSecurityListingStatisticsCalculator.save(currentStatistic);
            }
        });
    }

    private void setCriteriaNeededToAchieveCuresStatisticsForDate(LocalDate statisticDate) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (listingCriterionForCuresAchievementStatisticsCalculator.hasStatisticsForDate(statisticDate)) {
                    listingCriterionForCuresAchievementStatisticsCalculator.deleteStatisticsForDate(statisticDate);
                }
                List<ListingToCriterionForCuresAchievementStatistic> currentStatistics = listingCriterionForCuresAchievementStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                listingCriterionForCuresAchievementStatisticsCalculator.save(currentStatistics);
            }
        });
    }
}
