package gov.healthit.chpl.scheduler.job;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String A_1 = "170.315 (a)(1)";
    private static final String A_2 = "170.315 (a)(2)";
    private static final String A_3 = "170.315 (a)(3)";
    private static final String A_10 = "170.315 (a)(10)";
    private static final String A_13 = "170.315 (a)(13)";
    private static final String B_1 = "170.315 (b)(1)";
    private static final String B_2 = "170.315 (b)(2)";
    private static final String B_3 = "170.315 (b)(3)";
    private static final String E_1 = "170.315 (e)(1)";
    private static final String E_2 = "170.315 (e)(2)";
    private static final String G_8 = "170.315 (g)(8)";
    private static final String G_9 = "170.315 (g)(9)";

    private static Map<String, List<String>> measuresToRemove;
    static {
        measuresToRemove = new HashMap<String, List<String>>();
        measuresToRemove.put(A_1,
                Arrays.asList("EP Stage 2",
                        "EH/CAH Stage 2",
                        "GAP-EP",
                        "GAP-EH/CAH"));
        measuresToRemove.put(A_2,
                Arrays.asList("EP Stage 2",
                        "EH/CAH Stage 2",
                        "GAP-EP",
                        "GAP-EH/CAH"));
        measuresToRemove.put(A_3,
                Arrays.asList("EP Stage 2",
                        "EH/CAH Stage 2",
                        "GAP-EP",
                        "GAP-EH/CAH"));
        measuresToRemove.put(A_10,
                Arrays.asList("EP Stage 2",
                        "EC PI Transition",
                        "EH/CAH Stage 2"));
        measuresToRemove.put(A_13,
                Arrays.asList("EP Stage 2",
                        "EC PI Transition",
                        "EH/CAH Stage 2"));
        measuresToRemove.put(B_1,
                Arrays.asList("RT7 EP Stage 2",
                        "RT7 EC PI Transition",
                        "RT7 EH/CAH Stage 2"));
        measuresToRemove.put(B_2,
                Arrays.asList("EP Stage 2",
                        "EC PI Transition",
                        "EH/CAH Stage 2"));
        measuresToRemove.put(B_3,
                Arrays.asList("EP Stage 2",
                        "EC PI Transition",
                        "EH/CAH Stage 2"));
        measuresToRemove.put(E_1,
                Arrays.asList("RT2a EP Stage 2",
                        "RT2a EC PI Transition ",
                        "RT2a EH/CAH Stage 2",
                        "RT2b EP Stage 2",
                        "RT2b EC ACI Transition",
                        "RT2b EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4a EC ACI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4b EP Stage 2",
                        "RT4b EC PI Transition",
                        "RT4b EH/CAH Stage 2"));
        measuresToRemove.put(E_2,
                Arrays.asList("EP Stage 2",
                        "EC PI Transition",
                        "EH/CAH Stage 2"));
        measuresToRemove.put(G_8,
                Arrays.asList("RT2a EP Stage 2",
                        "RT2a EC PI Transition",
                        "RT2a EH/CAH Stage 2",
                        "RT2c EP Stage 2",
                        "RT2c EC PI Transition",
                        "RT2c EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4a EC PI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4c EP Stage 2",
                        "RT4c EC PI Transition",
                        "RT4c EH/CAH Stage 2"));
        measuresToRemove.put(G_9,
                Arrays.asList("RT2a EP Stage 2",
                        "RT2a EC PI Transition",
                        "RT2a EH/CAH Stage 2",
                        "RT2c EP Stage 2",
                        "RT2c EC PI Transition",
                        "RT2c EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4a EC PI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4c EP Stage 2",
                        "RT4c EC PI Transition",
                        "RT4c EH/CAH Stage 2"));
    }

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

                    updateMacraMeasuresDao.updateStage2AsRemoved();
                    LOGGER.info(SPACER + " Updated specific g8/g9 Macra Measures as removed. " + SPACER);
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

        private void updateStage2AsRemoved() {
            getAllMeasuresWhereValueIsNotNull().stream()
                    .filter(mm -> isThisAMacraMeasureToRemove(mm))
                    .forEach(measure -> {
                        measure.setRemoved(true);
                        entityManager.merge(measure);
                    });
        }

        private boolean isThisAMacraMeasureToRemove(MacraMeasureEntity mm) {
            if (measuresToRemove.containsKey(mm.getCertificationCriterion().getNumber())) {
                List<String> measures = measuresToRemove.get(mm.getCertificationCriterion().getNumber());
                return measures.contains(mm.getValue());
            }
            return false;
        }

        private List<MacraMeasureEntity> getAllMeasuresWhereValueIsNotNull() {
            Query query = entityManager
                    .createQuery("FROM MacraMeasureEntity mme "
                            + "JOIN FETCH mme.certificationCriterion cce "
                            + "WHERE mme.value IS NOT NULL "
                            + "AND mme.deleted = FALSE",
                            MacraMeasureEntity.class);
            return query.getResultList();
        }
    }
}
