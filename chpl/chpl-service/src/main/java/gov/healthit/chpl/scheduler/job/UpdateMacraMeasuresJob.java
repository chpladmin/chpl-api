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
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import net.sf.ehcache.CacheManager;

public class UpdateMacraMeasuresJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateMacraMeasuresJobLogger");

    private static final String SPACER = "*********";
    private static final String JOB_NAME = "Update Macra Measures";

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
    private static final String E_3 = "170.315 (e)(3)";
    private static final String G_8 = "170.315 (g)(8)";
    private static final String G_9 = "170.315 (g)(9)";

    @Autowired
    private CertificationCriterionService criteriaService;

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
                Arrays.asList("EC ACI Transition",
                        "EH/CAH Stage 2",
                        "EP Stage 2",
                        "RT13 EC",
                        "RT13 EH/CAH Stage 3"));
        measuresToRemove.put(A_13,
                Arrays.asList("EC ACI",
                        "EC ACI Transition",
                        "EH/CAH Stage 2",
                        "EP Stage 2"));
        measuresToRemove.put(B_1,
                Arrays.asList("RT7 EC ACI Transition",
                        "RT7 EH/CAH Stage 2",
                        "RT7 EP Stage 2",
                        "RT8 EC ACI"));
        measuresToRemove.put(B_2,
                Arrays.asList("EC ACI",
                        "EC ACI Transition",
                        "EH/CAH Stage 2",
                        "EP Stage 2"));
        measuresToRemove.put(B_3,
                Arrays.asList("EC ACI Transition",
                        "EH/CAH Stage 2",
                        "EP Stage 2",
                        "RT13 EC",
                        "RT13 EH/CAH Stage 3"));
        measuresToRemove.put(E_1,
                Arrays.asList("RT2a EC ACI Transition",
                        "RT2a EH/CAH Stage 2",
                        "RT2a EP Stage 2",
                        "RT2b EC ACI Transition",
                        "RT2b EH/CAH Stage 2",
                        "RT2b EP Stage 2",
                        "RT4a EC",
                        "RT4a EC ACI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4b EC",
                        "RT4b EC ACI Transition",
                        "RT4b EH/CAH Stage 2",
                        "RT4b EP Stage 2"));
        measuresToRemove.put(E_2,
                Arrays.asList("EC ACI",
                        "EC ACI Transition",
                        "EH/CAH Stage 2",
                        "EP Stage 2"));
        measuresToRemove.put(E_3,
                Arrays.asList("EC ACI"));
        measuresToRemove.put(G_8,
                Arrays.asList("RT2a EC ACI Transition",
                        "RT2a EH/CAH Stage 2",
                        "RT2a EP Stage 2",
                        "RT2c EC ACI Transition",
                        "RT2c EH/CAH Stage 2",
                        "RT2c EP Stage 2",
                        "RT4a EC ACI",
                        "RT4a EC ACI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4c EC ACI",
                        "RT4c EC ACI Transition",
                        "RT4c EH/CAH Stage 2",
                        "RT4c EP Stage 2"));
        measuresToRemove.put(G_9,
                Arrays.asList("RT2a EC ACI Transition",
                        "RT2a EH/CAH Stage 2",
                        "RT2a EP Stage 2",
                        "RT2c EC ACI Transition",
                        "RT2c EH/CAH Stage 2",
                        "RT2c EP Stage 2",
                        "RT4a EC ACI",
                        "RT4a EC ACI Transition",
                        "RT4a EH/CAH Stage 2",
                        "RT4a EP Stage 2",
                        "RT4c EC ACI",
                        "RT4c EC ACI Transition",
                        "RT4c EH/CAH Stage 2",
                        "RT4c EP Stage 2"));
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
                    LOGGER.info(SPACER + " Starting to update all g1g2 Values and Descriptions" + SPACER);
                    updateValues();
                    LOGGER.info(SPACER + " Updated all g1g2 Values and Descriptions" + SPACER);

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

    @SuppressWarnings({"checkstyle:linelength"})
    private void updateValues() {
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_1).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 10: Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_1).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 10: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_2).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 11: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_2).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 11: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_3).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 12: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_3).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 12: Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_10).getId(), "EC ACI", "RT 1 EC ", "Required Test 1: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_10).getId(), "EH/CAH Stage 3", "RT1 EH/CAH Medicare and Medicaid PI", "Required Test 1: Medicare and Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_10).getId(), "EP Stage 3", "RT1 EP Medicaid PI", "Required Test 1: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_10).getId(), "RT14 EC", "RT14 EC", "Required Test 14: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_10).getId(), "RT14 EH/CAH Stage 3", "RT14 EH/CAH Medicare PI", "Required Test 14: Medicare Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_13).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 3: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.A_13).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 3: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT15 EC", "RT15 EC", "Required Test 15: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT15 EH/CAH Stage 3", "RT15 EH/CAH Medicare PI", "Required Test 15: Medicare Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT7 EC ACI", "RT7 EC", "Required Test 7: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT7 EH/CAH Stage 3", "RT7 EH/CAH Medicare and Medicaid PI", "Required Test 7: Medicare and Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT7 EP Stage 3", "RT7 EP Medicaid PI", "Required Test 7: Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT8 EH/CAH Stage 3", "RT8 EH/CAH Medicaid PI", "Required Test 8: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_1_OLD).getId(), "RT8 EP Stage 3", "RT8 EP Medicaid PI", "Required Test 8: Medicaid Promoting Interoperability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_2_OLD).getId(), "EH/CAH Stage 3", "RT9 EH/CAH Medicaid PI", "Required Test 9: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_2_OLD).getId(), "EP Stage 3", "RT9 EP Medicaid PI", "Required Test 9: Medicaid Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_2_OLD).getId(), "RT15 EC", "RT15 EC", "Required Test 15: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_2_OLD).getId(), "RT15 EH/CAH Stage 3", "RT15 EH/CAH Medicare PI", "Required Test 15: Medicare Promoting Interoperability Progra");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_3_OLD).getId(), "EC ACI", "RT1 EC", "Required Test 1: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_3_OLD).getId(), "EH/CAH Stage 3", "RT1 EH/CAH Medicare and Medicaid PI", "Required Test 1: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_3_OLD).getId(), "EP Stage 3", "RT1 EP Medicaid PI", "Required Test 1: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_3_OLD).getId(), "RT14 EC", "RT14 EC", "Required Test 14: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.B_3_OLD).getId(), "RT14 EH/CAH Stage 3", "RT14 EH/CAH Medicare PI", "Required Test 14: Medicare Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2a EC ACI", "RT2a EC", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2a EH/CAH Stage 3", "RT2a EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2a EP Stage 3", "RT2a EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2b EC ACI", "RT2b EC", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2b EH/CAH Stage 3", "RT2b EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT2b EP Stage 3", "RT2b EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4a EH/CAH Stage 3", "RT4a EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4a EP Stage 3", "RT4a EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4b EH/CAH Stage 3", "RT4b EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4b EP Stage 3", "RT4b EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_2).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 5: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_2).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 5: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_3).getId(), "EH/CAH Stage 3", "EH/CAH Medicaid PI", "Required Test 6: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_3).getId(), "EP Stage 3", "EP Medicaid PI", "Required Test 6: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2a EC ACI", "RT2a EC", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2a EH/CAH Stage 3", "RT2a EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2a EP Stage 3", "RT2a EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2c EC ACI", "RT2c EC", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2c EH/CAH Stage 3", "RT2c EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT2c EP Stage 3", "RT2c EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT4a EH/CAH Stage 3", "RT4a EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT4a EP Stage 3", "RT4a EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT4c EH/CAH Stage 3", "RT4c EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_8).getId(), "RT4c EP Stage 3", "RT4c EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2a EC ACI", "RT2a EC ", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2a EH/CAH Stage 3", "RT2a EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2a EP Stage 3", "RT2a EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2c EC ACI", "RT2c EC", "Required Test 2: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2c EH/CAH Stage 3", "RT2c EH/CAH Medicare and Medicaid PI", "Required Test 2: Medicare and Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT2c EP Stage 3", "RT2c EP Medicaid PI", "Required Test 2: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT4a EH/CAH Stage 3", "RT4a EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT4a EP Stage 3", "RT4a EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT4c EH/CAH Stage 3", "RT4c EH/CAH Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.G_9_OLD).getId(), "RT4c EP Stage 3", "RT4c EP Medicaid PI", "Required Test 4: Medicaid Promoting Interopability Programs");
        // these last two will be removed, but need to be updated anyway
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4a EC ACI", "RT4a EC", "Required Test 4: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
        updateMacraMeasuresDao.updateValueAndDescription(criteriaService.get(Criteria2015.E_1_OLD).getId(), "RT4b EC ACI", "RT4b EC", "Required Test 4: Merit-based Incentive Payment System (MIPS) Promoting Interoperability Performance Category");
    }

    @Component("updateMacraMeasuresDao")
    private static class UpdateMacraMeasuresDao extends BaseDAOImpl {
        private void updateValueAndDescription(long criteriaId, String oldValue, String newValue, String newDescription) {
            MacraMeasureEntity measure = getByCriterionAndValue(criteriaId, oldValue);
            measure.setValue(newValue);
            measure.setDescription(newDescription);
            entityManager.merge(measure);
            LOGGER.info(SPACER + " Updated " + oldValue + " to " + newValue + " on criteria with id " + criteriaId + SPACER);
        }

        private MacraMeasureEntity getByCriterionAndValue(Long criterionId, String value) {
            Query query = entityManager.createQuery(
                    "FROM MacraMeasureEntity mme "
                            + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                            + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                            + "WHERE (NOT mme.deleted = true) "
                            + "AND cce.id = :criterionId "
                            + "AND (UPPER(mme.value) = :value)",
                    MacraMeasureEntity.class);
            query.setParameter("criterionId", criterionId);
            query.setParameter("value", value.trim().toUpperCase());
            List<MacraMeasureEntity> result = query.getResultList();
            if (result == null || result.size() == 0) {
                return null;
            }
            return result.get(0);
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
