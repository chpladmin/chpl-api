package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.standard.StandardManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UpdatedCriteriaStatusReportCreatorJob extends QuartzJob {

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private StandardManager standardManager;

    @Autowired
    private FunctionalityTestedManager functionalityTestedManager;

    @Autowired
    private UpdatedCriteriaStatusReportDAO updatedCriteriaStatusReportDAO;
    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Updated Listing Status Report Creator job. *********");
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
                    CertificationCriterionComparator certificationCriterionComparator = new CertificationCriterionComparator();
                    getCriteriaList().stream()
                    .filter(crit -> crit.getStatus() == CriterionStatus.ACTIVE)
                    .sorted(certificationCriterionComparator)
                    .forEach(criterion -> {
                        try {
                            List<ListingSearchResult> listingSearchResults = getActiveListingsAttestingToCriteria(criterion.getId());

                            UpdatedCriteriaStatusReport updatedCriteriaStatusReport = UpdatedCriteriaStatusReport.builder()
                                    .reportDay(LocalDate.now())
                                    .certificationCriterionId(criterion.getId())
                                    .listingsWithCriterionCount(listingSearchResults.size())
                                    .fullyUpToDateCount(0)
                                    .standardsUpToDateCount(0)
                                    .functionalitiesTestedUpToDateCount(0)
                                    .codeSetsUpToDateCount(0)
                                    .build();

                            getActiveListingsAttestingToCriteria(criterion.getId()).forEach(searchResult -> {
                                Optional<CertifiedProductSearchDetails> listing = getCertifiedProductDetails(searchResult.getId());

                                if (listing.isPresent()) {
                                    Boolean areStandardsUpdated = areStandardsUpdated(getCertificationResult(listing.get(), criterion.getId()));
                                    Boolean areFunctionalitiesTestedUpdated = areFunctionalitiesTestedUpdated(getCertificationResult(listing.get(), criterion.getId()));
                                    Boolean areCodeSetsUpdated = true;

                                    if (areStandardsUpdated && areFunctionalitiesTestedUpdated && areCodeSetsUpdated) {
                                        updatedCriteriaStatusReport.setFullyUpToDateCount(updatedCriteriaStatusReport.getFullyUpToDateCount() + 1);
                                    }
                                    if (areStandardsUpdated) {
                                        updatedCriteriaStatusReport.setStandardsUpToDateCount(updatedCriteriaStatusReport.getStandardsUpToDateCount() + 1);
                                    }
                                    if (areFunctionalitiesTestedUpdated) {
                                        updatedCriteriaStatusReport.setFunctionalitiesTestedUpToDateCount(updatedCriteriaStatusReport.getFunctionalitiesTestedUpToDateCount() + 1);
                                    }
                                    if (areCodeSetsUpdated) {
                                        updatedCriteriaStatusReport.setCodeSetsUpToDateCount(updatedCriteriaStatusReport.getCodeSetsUpToDateCount() + 1);
                                    }
                                }
                            });

                            LOGGER.info(updatedCriteriaStatusReport.toString());
                            updatedCriteriaStatusReportDAO.create(updatedCriteriaStatusReport);
                        } catch (ValidationException e) {
                            LOGGER.error("Could not process UpdatedCriteriaStatusReport - {}", e.getMessage(), e);
                        }
                    });
                }
            });
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed the Updated Listing Status Report Creator job. *********");
        }
    }

    private List<ListingSearchResult> getActiveListingsAttestingToCriteria(Long certificationCriterionId) throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(Set.of("Active", "Suspended by ONC", "Suspended by ONC-ACB"))
                .certificationCriteriaIds(Set.of(certificationCriterionId))
                .certificationCriteriaOperator(SearchSetOperator.AND)
                .build();

        return listingSearchService.getAllPagesOfSearchResults(request);
    }

    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long certifiedProductId) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(certifiedProductId));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing with id: {}", certifiedProductId, e);
            return Optional.empty();
        }
    }

    private boolean areStandardsUpdated(CertificationResult certificationResult) {
        Long updateRequired = 0L;
        if (CollectionUtils.isNotEmpty(certificationResult.getStandards())) {
            updateRequired = certificationResult.getStandards().stream()
                    .filter(mergedCertResultStd -> mergedCertResultStd.getStandard().getEndDay() != null)
                    .count();
        }
        return updateRequired > 0L;
    }

    private boolean areFunctionalitiesTestedUpdated(CertificationResult certificationResult) {
        Long updateRequired = 0L;
        if (CollectionUtils.isNotEmpty(certificationResult.getFunctionalitiesTested())) {
            updateRequired += certificationResult.getFunctionalitiesTested().stream()
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getEndDay() != null)
                    .count();
        }
        return updateRequired > 0L;
    }

    private CertificationResult getCertificationResult(CertifiedProductSearchDetails listing, Long certificationCriterionId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(certificationCriterionId))
                .findAny()
                .orElse(null);

    }

    private Set<CertificationCriterion> getCriteriaList() {
        Set<CertificationCriterion> mergedCriteria = standardManager.getCertificationCriteriaForStandards().stream().collect(Collectors.toSet());
        mergedCriteria.addAll(functionalityTestedManager.getCertificationCriteriaForFunctionalitiesTested());

        return mergedCriteria;
    }
}
