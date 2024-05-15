package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshotCalculator;
import lombok.extern.log4j.Log4j2;

//@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
@Log4j2
@DisallowConcurrentExecution
public class SummaryStatisticsCreatorJob extends QuartzJob {

    @Autowired
    private StatisticsSnapshotCalculator statisticsSnapshotCalculator;

    @Autowired
    private SummaryStatisticsDAO summaryStatisticsDAO;

    @Autowired
    private JpaTransactionManager txManager;

    public SummaryStatisticsCreatorJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Summary Statistics Creation job. *********");
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            StatisticsSnapshot statisticsSnapshot = statisticsSnapshotCalculator.getStatistics();
            saveStatisticsSnapshot(statisticsSnapshot);
        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception: " + e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Summary Statistics Creation job. *********");
    }

    public void saveStatisticsSnapshot(StatisticsSnapshot statisticsSnapshot)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    SummaryStatisticsEntity entity = new SummaryStatisticsEntity();
                    entity.setEndDate(new Date());
                    entity.setSummaryStatistics(getJson(statisticsSnapshot));
                    summaryStatisticsDAO.create(entity);
                } catch (Exception e) {
                    LOGGER.error("Could not save Summary Statistic entity", e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private String getJson(StatisticsSnapshot statisticsSnapshot) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(statisticsSnapshot);
    }
}
