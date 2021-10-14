package gov.healthit.chpl.scheduler.job.onetime.teststandardconversion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ff4j.FF4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "testStandardConversionJobLogger")
public class TestStandardConversionJob extends CertifiedProduct2015Gatherer implements Job {
    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private TestStandardConversionSpreadsheet testStandardConversionSpreadhseet;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Autowired
    private FF4j ff4j;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    private Map<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping> mappings;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Test Standard Conversion job. *********");
        if (ff4j.check(FeatureList.OPTIONAL_STANDARDS) && ff4j.check(FeatureList.OPTIONAL_STANDARDS_ERROR)) {
            try {
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
                            // This will control how many threads are used by the parallelStream.  By default parallelStream
                            // will use the # of processors - 1 threads.  We want to be able to limit this.
                            ForkJoinPool pool = new ForkJoinPool(threadCount);

                            List<CertifiedProductSearchDetails> listings = pool.submit(() -> getAll2015CertifiedProducts(LOGGER)).get();

                            mappings = testStandardConversionSpreadhseet.getTestStandard2OptionalStandardsMap();
                            LOGGER.info("Created " + mappings.size() + " mappings");

                            pool.submit(() -> listings.parallelStream()
                                    .forEach(listing -> updateListing(listing)));

                        } catch (Exception e) {
                            LOGGER.error("Error inserting listing validation errors. Rolling back transaction.", e);
                            status.setRollbackOnly();
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.catching(e);
            }
        } else {
            LOGGER.info("Flags are not in correct state to run job.");
        }
        LOGGER.info("********* Completed the Test Standard Conversion job. *********");
    }

    private void updateListing(CertifiedProductSearchDetails listing) {
        Boolean converted = listing.getCertificationResults().stream()
                .map(cr -> convertCertificationResult(cr))
                .reduce(Boolean.FALSE, Boolean::logicalOr);

        if (converted) {
            ListingUpdateRequest request = new ListingUpdateRequest(listing, "Test Standard to Optional Standards conversion", true);
            try {
                setSecurityContext();
                certifiedProductManager.update(request);
                LOGGER.info(String.format("Listing Id: %s was successfully converted.", listing.getId()));
            } catch (ValidationException e) {
                LOGGER.info("The following errors were found while updating listing: " + listing.getId());
                e.getErrorMessages().stream()
                        .forEach(err -> LOGGER.info(err));
            } catch (Exception e) {
                LOGGER.catching(e);
            }
        } else {
            LOGGER.info(String.format("Listing Id: %s did not require any conversion.", listing.getId()));
        }
    }

    private Boolean convertCertificationResult(CertificationResult cr) {
        Boolean converted = false;
        if (cr.getTestStandards() != null) {
            converted = cr.getTestStandards().stream()
                .map(crts -> convertTestStandardToOptionalStandards(cr, crts))
                .reduce(Boolean.FALSE, Boolean::logicalOr);

            // This will delete the test standard
            cr.setTestStandards(cr.getTestStandards().stream()
                    .filter(crts -> !getMapping(cr.getCriterion(), getTestStandard(crts)).isPresent())
                    .collect(Collectors.toList()));
        }
        return converted;
    }

    private Boolean convertTestStandardToOptionalStandards(CertificationResult cr, CertificationResultTestStandard crts) {
        Boolean converted = false;
        Optional<TestStandard2OptionalStandardsMapping> mapping = getMapping(cr.getCriterion(), getTestStandard(crts));
        if (mapping.isPresent()) {
            converted = true;
            for (OptionalStandard optionalStandard : mapping.get().getOptionalStandards()) {
                cr.getOptionalStandards().add(CertificationResultOptionalStandard.builder()
                        .optionalStandardId(optionalStandard.getId())
                        .citation(optionalStandard.getCitation())
                        .description(optionalStandard.getDescription())
                        .build());
            }
        }
        return converted;
    }

    private Optional<TestStandard2OptionalStandardsMapping> getMapping(CertificationCriterion criterion, TestStandard testStandard) {
        Pair<CertificationCriterion, TestStandard> key = new ImmutablePair<CertificationCriterion, TestStandard>(criterion, testStandard);
        return Optional.ofNullable(mappings.get(key));
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(User.ADMIN_USER_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private TestStandard getTestStandard(CertificationResultTestStandard crts) {
        return TestStandard.builder()
                .id(crts.getTestStandardId())
                .name(crts.getTestStandardName())
                .description(crts.getTestStandardDescription())
                .year(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                .build();
    }
}
