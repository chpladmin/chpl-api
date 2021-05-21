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

import gov.healthit.chpl.dto.statistics.CriterionListingCountStatisticDTO;
import gov.healthit.chpl.dto.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticDTO;
import gov.healthit.chpl.dto.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticDTO;
import gov.healthit.chpl.dto.statistics.ListingCuresStatusStatisticDTO;
import gov.healthit.chpl.dto.statistics.PrivacyAndSecurityListingStatisticDTO;
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

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Statistics Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.ofDays(ONE_DAY));
        LOGGER.info("Calculating statistics for " + yesterday);

        //setCriterionListingCountStatisticsForDate(yesterday);
        setOriginalCriterionActivityStatisticsForDate(yesterday);
        setCuresCriterionActivityStatisticsForDate(yesterday);
        //setListingCuresStatusStatisticsForDate(yesterday);
        //setPrivacyAndSecurityListingStatisticsForDate(yesterday);
        //TODO: other statistic types

        LOGGER.info("*****Cures Reporting Statistics Job is complete.*****");
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
                List<CriterionListingCountStatisticDTO> currentStatistics = criterionListingStatisticsCalculator.calculateCurrentStatistics(statisticDate);
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
                List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> currentStatistics = originalCriterionActivityStatisticsCalculator.calculateCurrentStatistics(statisticDate);
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
                List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> currentStatistics = curesCriterionActivityStatisticsCalculator.calculateCurrentStatistics(statisticDate);
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
                ListingCuresStatusStatisticDTO currentStatistic = listingCuresStatusStatisticsCalculator.calculateCurrentStatistics(statisticDate);
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
                PrivacyAndSecurityListingStatisticDTO currentStatistic = privacyAndSecurityListingStatisticsCalculator.calculateCurrentStatistics(statisticDate);
                privacyAndSecurityListingStatisticsCalculator.save(currentStatistic);
            }
        });
    }
}
