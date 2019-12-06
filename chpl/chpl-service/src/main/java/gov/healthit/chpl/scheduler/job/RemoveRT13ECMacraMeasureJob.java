package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public class RemoveRT13ECMacraMeasureJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("removeRT13ECMacraMeasureJobLogger");

    private static final String RT13_EC = "RT13 EC";

    @Autowired
    private MacraMeasureDAO macraMeasureDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private JpaTransactionManager txnMgr;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove RT13 EC Macra Measure job. *********");

        // Since there is no manager, we need to handle the transaction.
        // @Transactional does not work in Quartz jobs, since Spring context
        // is injected after method has started.
        TransactionTemplate txnTemplate = new TransactionTemplate(txnMgr);
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    macraMeasureDAO.removeAllByValue(RT13_EC);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    status.setRollbackOnly();
                }
            }
        });

        // Need to "refresh" the data in CertifiedProductDetailsManager since it is stored within the bean.
        certifiedProductDetailsManager.refreshData();

        LOGGER.info("********* Completed the Remove RT13 EC Macra Measure job. *********");
    }
}
