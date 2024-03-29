package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attribute.AttributeType;
import gov.healthit.chpl.attribute.AttributeUpToDate;
import gov.healthit.chpl.attribute.AttributeUpToDateService;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.SearchRequest;
import lombok.extern.log4j.Log4j2;
@Log4j2(topic = "updatedListingStatusReportCreatorJobLogger")
public class UpdatedListingStatusReportCreatorJob extends QuartzJob {

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private UpdatedListingStatusReportDAO updatedListingStatusReportDAO;

    @Autowired
    private AttributeUpToDateService attributeUpToDateService;

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
                    try {
                        if (doStatisticsExistForDate(LocalDate.now())) {
                            deleteStatisticsForDate(LocalDate.now());
                        }
                        calculateStatisticsForActiveListings();
                    } catch (ValidationException e) {
                        LOGGER.error(e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed the Updated Listing Status Report Creator job. *********");
        }
    }

    private void calculateStatisticsForActiveListings() throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(Set.of("Active", "Suspended by ONC", "Suspended by ONC-ACB"))
                .build();

        listingSearchService.getAllPagesOfSearchResults(request).stream()
                .map(result -> getCertifiedProductDetails(result.getId()))
                .filter(listing -> listing.isPresent())
                .peek(x -> LOGGER.info("Processing {}", x.get().getChplProductNumber()))
                .map(certifiedProductDetails -> calculateUpdatedListingStatusReport(certifiedProductDetails.get()))
                .forEach(updatedListingStatusReport -> updatedListingStatusReportDAO.create(updatedListingStatusReport));
    }

    private UpdatedListingStatusReport calculateUpdatedListingStatusReport(CertifiedProductSearchDetails certifiedProductDetails) {
        Long criteriaRequireUpdateCount = getCriteriaRequireUpdateCount(certifiedProductDetails);
        LOGGER.info("Criteria requiring update count: {}", criteriaRequireUpdateCount);
        Long daysUpdatedEarly = 0L;
        if (criteriaRequireUpdateCount.equals(0L)) {
            daysUpdatedEarly = getDaysUpdatedEarly(certifiedProductDetails);
            LOGGER.info("Days updated early: {}", daysUpdatedEarly);
        }


        return UpdatedListingStatusReport.builder()
            .certifiedProductId(certifiedProductDetails.getId())
            .criteriaRequireUpdateCount(criteriaRequireUpdateCount)
            .daysUpdatedEarly(daysUpdatedEarly)
            .chplProductNumber(certifiedProductDetails.getChplProductNumber())
            .product(certifiedProductDetails.getProduct().getName())
            .version(certifiedProductDetails.getVersion().getVersion())
            .developer(certifiedProductDetails.getDeveloper().getName())
            .certificationBody(certifiedProductDetails.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString())
            .certificationStatus(certifiedProductDetails.getCurrentStatus().getStatus().getName())
            .developerId(certifiedProductDetails.getDeveloper().getId())
            .certificationBodyId(Long.valueOf(certifiedProductDetails.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()))
            .certificationStatusId(certifiedProductDetails.getCurrentStatus().getStatus().getId())
            .build();
    }



    private Long getCriteriaRequireUpdateCount(CertifiedProductSearchDetails certifiedProductDetails) {
        return certifiedProductDetails.getCertificationResults().stream()
                .filter(certResult -> !certResult.getCriterion().isRemoved()
                        && !isCriteriaUpdated(certResult))
                .count();
    }


    private boolean isCriteriaUpdated(CertificationResult certificationResult) {
        AttributeUpToDate standardsUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.STANDARDS, certificationResult, LOGGER);

        AttributeUpToDate functionalitiesTestedUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.FUNCTIONALITIES_TESTED, certificationResult, LOGGER);

        AttributeUpToDate codeSetsUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.CODE_SETS, certificationResult, LOGGER);

        if (standardsUpToDate.getEligibleForAttribute() && !standardsUpToDate.getUpToDate()) {
            return false;
        }
        if (functionalitiesTestedUpToDate.getEligibleForAttribute() && !functionalitiesTestedUpToDate.getUpToDate()) {
            return false;
        }
        if (codeSetsUpToDate.getEligibleForAttribute() && !codeSetsUpToDate.getUpToDate()) {
            return false;
        }
        LOGGER.info("{} is up to date", certificationResult.getCriterion().getNumber());
        return true;
    }

    private Long getDaysUpdatedEarly(CertifiedProductSearchDetails certifiedProductDetails) {
        return certifiedProductDetails.getCertificationResults().stream()
                .filter(certResult -> !certResult.getCriterion().isRemoved())
                .mapToLong(certResult -> getDaysUpdatedEarlyForCriteria(certResult))
                .filter(daysEarly -> daysEarly > 0)
                .min()
                .orElse(0L);
    }

    private Long getDaysUpdatedEarlyForCriteria(CertificationResult certificationResult) {
        AttributeUpToDate standardsUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.STANDARDS, certificationResult, LOGGER);

        AttributeUpToDate functionalitiesTestedUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.FUNCTIONALITIES_TESTED, certificationResult, LOGGER);

        AttributeUpToDate codeSetsUpToDate = attributeUpToDateService.getAttributeUpToDate(
                AttributeType.CODE_SETS, certificationResult, LOGGER);

        OptionalLong standardsDaysEarly = standardsUpToDate.getDaysUpdatedEarly();
        OptionalLong functionalityTestedDaysEarly = functionalitiesTestedUpToDate.getDaysUpdatedEarly();
        OptionalLong codeSetsDaysEarly = codeSetsUpToDate.getDaysUpdatedEarly();


        return List.of(standardsDaysEarly, functionalityTestedDaysEarly, codeSetsDaysEarly).stream()
                .filter(OptionalLong::isPresent)
                .map(OptionalLong::getAsLong)
                .map(Long::valueOf)
                .reduce(Long::min)
                .orElse(0L);
    }

    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long id) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(id));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing with id: {}", id, e);
            return Optional.empty();
        }
    }

    private Boolean doStatisticsExistForDate(LocalDate dateToCheck) {
        return updatedListingStatusReportDAO.getUpdatedListingStatusReportsByDate(dateToCheck).size() > 0;
    }

    private void deleteStatisticsForDate(LocalDate dateToCheck) {
        updatedListingStatusReportDAO.deleteUpdatedListingStatusReportsByDate(dateToCheck);
    }
}