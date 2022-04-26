package gov.healthit.chpl.scheduler.job.onetime.conformancemethodconversion;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestProcedureEntity;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "conformanceMethodConversionJobLogger")
public class ConformanceMethodConversionJob extends CertifiedProduct2015Gatherer implements Job {
    private static final String WILDCARD = "*";

    @Autowired
    private CertificationCriterionService certificationCriterionService;

    @Autowired
    private ConformanceMethodConversionDAO conformanceMethodConversionDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private FF4j ff4j;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    private List<ConversionRule> conversionRules;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Conformance Method Conversion job. *********");

        if (ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            conversionRules = getConversionRules();

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

                        getAll2015CertifiedProducts(LOGGER, threadCount, false).stream()
                            .forEach(listing -> convertListing(listing));

                    } catch (Exception e) {
                        LOGGER.error(e);
                    }
                }
            });
        } else {
            LOGGER.info("Could not run job - 'conformance-method' flag is not on.");
        }
        LOGGER.info("********* Completed the Conformance Method Conversion job. *********");
    }

    private void convertListing(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess())
                .forEach(cr -> convertCertificationResult(listing, cr));
    }

    private void convertCertificationResult(CertifiedProductSearchDetails listing, CertificationResult result) {
        List<ConversionRule> applicableConversionRules = conversionRules.stream()
                .filter(rule -> rule.getCriterion().equals(result.getCriterion()))
                .toList();

        applicableConversionRules.stream()
                .forEach(rule -> result.getTestProcedures().stream()
                        .forEach(crtp -> convertTestProcedureToConformanceMethod(listing, result, crtp, rule)));
    }

    private void convertTestProcedureToConformanceMethod(CertifiedProductSearchDetails listing, CertificationResult cr,  CertificationResultTestProcedure crtp, ConversionRule rule) {
        if (doesTestProcedureMatchRule(crtp, cr.isGap(), rule)) {
            deleteCertificationResultTestProcedure(crtp);
            addCertificationResultConformanceMethod(cr, rule.getConformanceMethodName(), crtp.getTestProcedureVersion());
            LOGGER.info(listing.getChplProductNumber() + " | "
                    + String.format("Criterion: %s - Convert TP(%s) to CM(%s)",
                            CertificationCriterionService.formatCriteriaNumber(cr.getCriterion()),
                            crtp.getTestProcedure().getName(),
                            rule.getConformanceMethodName()));
        }
    }


    private void deleteCertificationResultTestProcedure(CertificationResultTestProcedure crtp) {
        conformanceMethodConversionDAO.deleteCertificationResultTestProcedure(crtp.getId());
    }

    private void addCertificationResultConformanceMethod(CertificationResult cr, String conformanceMethodName, String version) {
        conformanceMethodConversionDAO.addCertificationResultConformanceMethod(cr.getId(), conformanceMethodName, version);
    }

    private Boolean doesTestProcedureMatchRule(CertificationResultTestProcedure crtp, Boolean hasGap, ConversionRule rule) {
        Boolean gapCheck = rule.getHasGap() == null ? true : hasGap == rule.getHasGap();
        return StringUtils.isNotEmpty(crtp.getTestProcedure().getName())
                && gapCheck
                && (rule.getTestProcedureName().equals(WILDCARD)
                        || rule.getTestProcedureName().equals(crtp.getTestProcedure().getName()));
    }

    private List<ConversionRule> getConversionRules() {
        return List.of(
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_1), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_2), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_3), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_4), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_5), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_6), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_7), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_8), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_9), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_10), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_11), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_12), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_13), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_14), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.A_15), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_OLD), "ONC Test Method", "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_OLD), "ONC Test Method - Surescripts(Alternative)", "Surescripts", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_4), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_5), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_6), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_1), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_2), "ONC Test Method", "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_2), "NCQA eCQM Test Method", "NCQA eCQM Test Method", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_OLD), "ONC Test Method", "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_OLD), "NCQA eCQM Test Method", "NCQA eCQM Test Method", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_CURES), "ONC Test Method", "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_4), "ONC Test Method", "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_4), "NCQA eCQM Test Method", "NCQA eCQM Test Method", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_1), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_OLD), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_CURES), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_OLD), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_CURES), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_4), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_5), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_6), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_7), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_8), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_9), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_OLD), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_CURES), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_11), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_12), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_13), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_2), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_3), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_1), "ONC Test Method", "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_1), "HIMSS-IIP Test Method", "HIMSS-IIP Test Method", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_2), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_3), "ONC Test Method", "Attestation", true),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_3), "ONC Test Method", "ONC Test Procedure", false),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_4), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_OLD), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_CURES), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_6), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_7), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_1), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_2), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_3), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_4), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_5), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_7), WILDCARD, "Attestation", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_8), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_OLD), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_CURES), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.H_1), WILDCARD, "ONC Test Procedure", null),
                new ConversionRule(certificationCriterionService.get(CertificationCriterionService.Criteria2015.H_2), WILDCARD, "ONC Test Procedure", null));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class ConversionRule {
        private CertificationCriterion criterion;
        private String testProcedureName;
        private String conformanceMethodName;
        private Boolean hasGap;

        @Override
        public String toString() {
            return String.format("Criterion: %s - Convert TP(%s) to CM(%s)", criterion.getNumber(), testProcedureName, conformanceMethodName);
        }
    }

    @Component
    private static class ConformanceMethodConversionDAO extends BaseDAOImpl {
        public void deleteCertificationResultTestProcedure(Long certificationResultTestProcedureId) {
            Query query = entityManager.createQuery(
                    "SELECT crtpe "
                    + "FROM CertificationResultTestProcedureEntity crtpe "
                    + "WHERE crtpe.deleted = false "
                    + "AND crtpe.id = :id ",
                    CertificationResultTestProcedureEntity.class);
            query.setParameter("id", certificationResultTestProcedureId);
            List<CertificationResultTestProcedureEntity> result = query.getResultList();
            if (result != null && result.size() == 1) {
                result.get(0).setDeleted(true);
                update(result.get(0));
            }
        }

        public void addCertificationResultConformanceMethod(Long certificationResultId, String conformanceMethod, String version) {
            CertificationResultConformanceMethodEntity entity = new CertificationResultConformanceMethodEntity();
            entity.setCertificationResultId(certificationResultId);
            entity.setConformanceMethodId(getConformanceMethodId(conformanceMethod));
            entity.setVersion(conformanceMethod.equals("Attestation") ? "" : version);
            entity.setCreationDate(new Date());
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(User.DEFAULT_USER_ID);
            entity.setDeleted(false);
            create(entity);
        }

        private Long getConformanceMethodId(String name) {
            Query query = entityManager.createQuery(
                    "SELECT cme "
                    + "FROM ConformanceMethodEntity cme "
                    + "WHERE cme.deleted = false "
                    + "AND cme.name = :name ",
                    ConformanceMethodEntity.class);
            query.setParameter("name", name);
            List<ConformanceMethodEntity> result = query.getResultList();
            if (result != null && result.size() == 1) {
                return result.get(0).getId();
            } else {
                return null;
            }
        }
    }
}
