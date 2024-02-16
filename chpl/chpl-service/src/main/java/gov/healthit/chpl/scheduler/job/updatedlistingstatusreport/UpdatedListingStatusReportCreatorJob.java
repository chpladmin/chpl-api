package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

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

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.CertificationResultStandardDAO;
import gov.healthit.chpl.util.DateUtil;
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
    private CertificationResultStandardDAO certificationResultStandardDAO;

    @Autowired
    private CertificationResultFunctionalityTestedDAO certificationResultFunctionalityTestedDAO;

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
        Long daysUpdatedEarly = 0L;
        if (criteriaRequireUpdateCount.equals(0L)) {
            daysUpdatedEarly = getDaysUpdatedEarly(certifiedProductDetails);
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
                .filter(certResult -> isCriteriaUpdated(certResult))
                .peek(x -> LOGGER.info(x.getCriterion().getNumber()))
                .count();
    }

    private boolean isCriteriaUpdated(CertificationResult certificationResult) {
        Long updateRequired = 0L;

        if (CollectionUtils.isNotEmpty(certificationResult.getStandards())) {
            updateRequired = certificationResult.getStandards().stream()
                    .filter(mergedCertResultStd -> mergedCertResultStd.getStandard().getEndDay() != null)
                    .count();
        }
        if (CollectionUtils.isNotEmpty(certificationResult.getFunctionalitiesTested())) {
            updateRequired += certificationResult.getFunctionalitiesTested().stream()
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getEndDay() != null)
                    .count();
        }
        // TODO - Will need to determine for HTI-2 how to correctly handle this.  Possible
        // future state is we will need to make sure the most recent and in the past codes set is
        // attested to.
        // Initially, we will just make sure that the cert result has attested to the same
        // number of code sets as are available for the criteria.
        //if (CollectionUtils.isNotEmpty(certificationResult.getCodeSetDates())) {
        //    updateRequired += certificationResult.getCodeSetDates().stream()
        //            .filter(certResultCsd -> certResultCsd.getCodeSetDate().getEndDay() != null)
        //            .count();
        //}
        return updateRequired > 0L;
    }

    private Long getDaysUpdatedEarly(CertifiedProductSearchDetails certifiedProductDetails) {
        return certifiedProductDetails.getCertificationResults().stream()
                .mapToLong(certResult -> getDaysUpdatedEarlyForCriteria(certResult))
                .filter(daysEarly -> daysEarly > 0)
                .min()
                .orElse(0L);
    }

    private Long getDaysUpdatedEarlyForCriteria(CertificationResult certificationResult) {
        OptionalLong standardsDaysEarly = getDaysUpdatedEarlyForCriteriaBasedOnStandards(certificationResult);
        OptionalLong functionalityTestedDaysEarly = getDaysUpdatedEarlyForCriteriaBasedOnFunctionalitiesTested(certificationResult);

        if (standardsDaysEarly.isPresent() && functionalityTestedDaysEarly.isEmpty()) {
            return standardsDaysEarly.getAsLong();
        } else if (standardsDaysEarly.isEmpty() && functionalityTestedDaysEarly.isPresent()) {
            return functionalityTestedDaysEarly.getAsLong();
        } else if (standardsDaysEarly.isPresent() && functionalityTestedDaysEarly.isPresent()) {
            return standardsDaysEarly.getAsLong() > functionalityTestedDaysEarly.getAsLong() ? functionalityTestedDaysEarly.getAsLong() : standardsDaysEarly.getAsLong();
        } else {
            return 0L;
        }
    }

    private OptionalLong getDaysUpdatedEarlyForCriteriaBasedOnStandards(CertificationResult certificationResult) {
        //Get the CertificationResultStandards using DAO, so that we have the create date
        List<CertificationResultStandard> certificationResultStandards = certificationResultStandardDAO.getStandardsForCertificationResult(certificationResult.getId());
        OptionalLong daysUpdatedEarly = OptionalLong.empty();
        if (CollectionUtils.isNotEmpty(certificationResultStandards)) {
            daysUpdatedEarly = certificationResultStandards.stream()
                    .filter(certResultStd -> certResultStd.getStandard().getRequiredDay() != null
                            && LocalDate.now().isBefore(certResultStd.getStandard().getRequiredDay())
                            && DateUtil.toLocalDate(certResultStd.getCreationDate().getTime()).isBefore(certResultStd.getStandard().getRequiredDay()))
                    .mapToLong(certResultStd -> ChronoUnit.DAYS.between(DateUtil.toLocalDate(certResultStd.getCreationDate().getTime()), certResultStd.getStandard().getRequiredDay()))
                    .min();

            LOGGER.info("Standards Check {} - {}", certificationResult.getCriterion().getNumber(), daysUpdatedEarly);
        }
        return daysUpdatedEarly;
    }

    private OptionalLong getDaysUpdatedEarlyForCriteriaBasedOnFunctionalitiesTested(CertificationResult certificationResult) {
        //Get the CertificationResultFunctionalitiesTested using DAO, so that we have the create date
        List<CertificationResultFunctionalityTested> certificationResultFunctionalitiesTested =
                certificationResultFunctionalityTestedDAO.getFunctionalitiesTestedForCertificationResult(certificationResult.getId());

        OptionalLong daysUpdatedEarly = OptionalLong.empty();
        if (CollectionUtils.isNotEmpty(certificationResultFunctionalitiesTested)) {
            daysUpdatedEarly = certificationResultFunctionalitiesTested.stream()
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getRequiredDay() != null
                            && LocalDate.now().isBefore(certResultFT.getFunctionalityTested().getRequiredDay())
                            && DateUtil.toLocalDate(certResultFT.getCreationDate().getTime()).isBefore(certResultFT.getFunctionalityTested().getRequiredDay()))
                    .mapToLong(certResultFT -> ChronoUnit.DAYS.between(DateUtil.toLocalDate(certResultFT.getCreationDate().getTime()), certResultFT.getFunctionalityTested().getRequiredDay()))
                    .min();

            LOGGER.info("FT Check {} - {}", certificationResult.getCriterion().getNumber(), daysUpdatedEarly);
        }
        return daysUpdatedEarly;
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