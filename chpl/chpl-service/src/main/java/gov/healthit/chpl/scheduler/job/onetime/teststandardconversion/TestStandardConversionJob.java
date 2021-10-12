package gov.healthit.chpl.scheduler.job.onetime.teststandardconversion;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestStandardConversionJob extends CertifiedProduct2015Gatherer implements Job {
    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private TestStandardConversionSpreadsheet testStandardConversionSpreadhseet;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Test Standard Conversion job. *********");
        try {
            // We need to manually create a transaction in this case because of how AOP works. When a method is
            // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
            // The object's proxy is not called when the method is called from within this class. The object's proxy
            // is called when the method is public and is called from a different object.
            // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
//            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
//            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//            txTemplate.execute(new TransactionCallbackWithoutResult() {
//                @Override
//                protected void doInTransactionWithoutResult(TransactionStatus status) {
//                    try {
//                        // This will control how many threads are used by the parallelStream.  By default parallelStream
//                        // will use the # of processors - 1 threads.  We want to be able to limit this.
//                        ForkJoinPool pool = new ForkJoinPool(threadCount);
//                        List<CertifiedProductSearchDetails> listings = pool.submit(() -> getAll2015CertifiedProducts(LOGGER)).get();
//
//                    } catch (Exception e) {
//                        LOGGER.error("Error inserting listing validation errors. Rolling back transaction.", e);
//                        status.setRollbackOnly();
//                    }
//                }
//            });

            Map<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping> mappings = testStandardConversionSpreadhseet.getTestStandard2OptionalStandardsMap();
            LOGGER.info("Created " + mappings.size() + " mappings");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Test Standard Conversion job. *********");
    }
}
