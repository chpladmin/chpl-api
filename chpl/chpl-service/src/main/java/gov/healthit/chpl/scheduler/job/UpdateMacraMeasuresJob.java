package gov.healthit.chpl.scheduler.job;

import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.MacraMeasureEntity;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import net.sf.ehcache.CacheManager;

public class UpdateMacraMeasuresJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateMacraMeasuresJobLogger");

    private static final String SPACER = "*********";
    private static final String JOB_NAME = "Update Macra Measures";

    private static final String OLD_G1G2_SUBSTRING = "ACI";
    private static final String NEW_G1G2_SUBSTRING = "PI";

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private PendingCertifiedProductManager pcpManager;

    @Autowired
    private JpaTransactionManager txnMgr;

    @Autowired
    private UpdateMacraMeasuresDao updateMacraMeasuresDao;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info(SPACER + " Started the " + JOB_NAME + " job " + SPACER);

        TransactionTemplate txnTemplate = new TransactionTemplate(txnMgr);
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    updateMacraMeasuresDao.updateSubstringInAllValues(OLD_G1G2_SUBSTRING, NEW_G1G2_SUBSTRING);
                    LOGGER.info(SPACER + " Updated all g1g2 '" + OLD_G1G2_SUBSTRING
                            + "' MACRA measures to '" + NEW_G1G2_SUBSTRING + "' " + SPACER);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    status.setRollbackOnly();
                }
            }
        });

        CacheManager.getInstance().clearAll();
        certifiedProductDetailsManager.refreshData();
        pcpManager.refreshData();
        LOGGER.info(SPACER + " Completed the " + JOB_NAME + " job " + SPACER);
    }

    @Component("updateMacraMeasuresDao")
    private static class UpdateMacraMeasuresDao extends BaseDAOImpl {
        private void updateSubstringInAllValues(final String oldSubstring, final String newSubstring) {
            getAllMeasuresWhereValueIsNotNull().stream()
                .forEach(measure -> {
                    measure.setValue(measure.getValue().replaceAll(oldSubstring, newSubstring));
                    entityManager.merge(measure);
                });
        }

        private List<MacraMeasureEntity> getAllMeasuresWhereValueIsNotNull() {
            Query query = entityManager
                    .createQuery("FROM MacraMeasureEntity mme "
                            + "WHERE mme.value IS NOT NULL "
                            + "AND mme.deleted = FALSE",
                            MacraMeasureEntity.class);
            return query.getResultList();
        }
    }
}
