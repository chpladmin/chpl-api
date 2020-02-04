package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public class UpdateMacraMeasuresJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateMacraMeasuresJobLogger");

    private static final String SPACER = "*********";
    private static final String JOB_NAME = "Update Macra Measures";

    private static final String OLD_G1G2_SUBSTRING = "ACI";
    private static final String NEW_G1G2_SUBSTRING = "PI";

    @Autowired
    private MacraMeasureDAO macraMeasureDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private JpaTransactionManager txnMgr;

    @Autowired
    private FF4j ff4j;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            LOGGER.info("The " + FeatureList.EFFECTIVE_RULE_DATE + " flag is off. "
                    + "Macra Measueres will not be updated.");
        } else {
            LOGGER.info(SPACER + " Started the " + JOB_NAME + " job " + SPACER);
            
            TransactionTemplate txnTemplate = new TransactionTemplate(txnMgr);
            txnTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        macraMeasureDAO.updateSubstringInAllValues(OLD_G1G2_SUBSTRING, NEW_G1G2_SUBSTRING);
                        LOGGER.info(SPACER + " Updated all g1g2 '" + OLD_G1G2_SUBSTRING
                                + "' MACRA measures to '" + NEW_G1G2_SUBSTRING + "' " + SPACER);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        status.setRollbackOnly();
                    }
                }
            });

            certifiedProductDetailsManager.refreshData();
            LOGGER.info(SPACER + " Completed the " + JOB_NAME + " job " + SPACER);
        }
    }
}
