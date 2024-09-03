package gov.healthit.chpl.scheduler.job.onetime.sed;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;
import gov.healthit.chpl.validation.listing.Edition2015ListingValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "updatedSedFriendlyIdsJobLogger")
public class UpdateSedFriendlyIdsJob implements Job {
    private static final String FAILURE_TO_UPDATE_MSG = "Listing % SED friendly IDs could not be updated.";
    private static final long FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD = 10912;

    @Autowired
    private ReprocessFromUploadedCsvStrategy reprocessFromUploadStrategy;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private CertificationCriterionService criteriaService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private Edition2015ListingValidator listingValidator;

    @Autowired
    private SharedListingStoreProvider sharedStoreProvider;

    @Autowired
    private SedFriendlyIdReplacementDao sedFriendlyIdReplacementDao;

    private CertificationCriterion g3 = null;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Update Participants job. *********");
        try {
            g3 = criteriaService.get(Criteria2015.G_3);

            List<ListingSearchResult> activeListingsWithG3ConfirmedWithFlexibleUpload = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationCriteriaIds(Stream.of(g3.getId()).collect(Collectors.toSet()))
                    .certificationCriteriaOperator(SearchSetOperator.AND)
                    .build()).stream()
                    .filter(listingSearchResult -> listingSearchResult.getId() >= FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD)
                    .toList();

            LOGGER.info("Found " + activeListingsWithG3ConfirmedWithFlexibleUpload.size() + " listing uploads attesting to 170.315 (g)(3).");

            activeListingsWithG3ConfirmedWithFlexibleUpload.stream()
                .forEach(listing -> attemptToSaveFriendlyIds(listing));

        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }
        LOGGER.info("********* Completed the Update Participants job. *********");
    }

    private void attemptToSaveFriendlyIds(ListingSearchResult listing) {
        LOGGER.info("Processing listing ID " + listing.getId());
        CertifiedProductSearchDetails listingDetails = getCertifiedProductSearchDetails(listing.getId());
        LOGGER.info("Updating test task friendly IDs for listing ID " + listing.getId());
        ListingUpdateResult listingUpdateResult = updateTaskFriendlyIds(listingDetails);
        listingDetails = getCertifiedProductSearchDetails(listingDetails.getId());
        LOGGER.info("Updating test participant friendly IDs for listing ID " + listing.getId());
        listingUpdateResult = updateParticipantFriendlyIds(listingDetails);

        if (listingUpdateResult.getUpdatedListing() != null) {
            listingValidator.validate(listingUpdateResult.getOriginalListing(), listingUpdateResult.getUpdatedListing());
            if (hasSedErrors(listingUpdateResult.getUpdatedListing())) {
                LOGGER.warn("\tThe updated listing has SED-related errors or warnings so we will not save SED changes: ");
                printListingErrorsAndWarnings(listingUpdateResult.getUpdatedListing());
                LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
            } else {
                //this replacement should execute as a single transaction in case any part of it fails
                try {
                    sedFriendlyIdReplacementDao.updateSedFriendlyIds(listingUpdateResult.getUpdatedListing());
                    LOGGER.info("Completed updating SED friendly IDs for listing " + listingUpdateResult.getUpdatedListing().getId());
                } catch (Exception ex) {
                    LOGGER.error("Error updating SED friendly IDs for listing " + listingUpdateResult.getUpdatedListing().getId(), ex);
                    LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
                } finally {
                    sharedStoreProvider.remove(listingUpdateResult.getUpdatedListing().getId());
                }
            }
        } else {
            LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
        }
    }

    private ListingUpdateResult updateTaskFriendlyIds(CertifiedProductSearchDetails listing) {
        ListingUpdateResult result = new ListingUpdateResult();
        result.setOriginalListing(getCertifiedProductSearchDetails(listing.getId()));
        if (reprocessFromUploadStrategy.updateTasks(listing)) {
            result.setUpdatedListing(listing);
            LOGGER.info("\tTasks for listing " + listing.getId() + " were updated.");
        }
        return result;
    }

    private ListingUpdateResult updateParticipantFriendlyIds(CertifiedProductSearchDetails listing) {
        ListingUpdateResult result = new ListingUpdateResult();
        //get the details again for the "original" listing because the strategies will change the listing reference
        result.setOriginalListing(getCertifiedProductSearchDetails(listing.getId()));
        if (reprocessFromUploadStrategy.updateParticipants(listing)) {
            result.setUpdatedListing(listing);
            LOGGER.info("\tParticipants for listing " + listing.getId() + " were updated.");
        }
        return result;
    }

    private boolean hasSedErrors(CertifiedProductSearchDetails listing) {
        //there are probably errors and warnings that weren't here originally -
        //some criteria have been removed, we changed measure parsing, etc
        return !CollectionUtils.isEmpty(listing.getErrorMessages().castToCollection())
                && isAnyMessageAboutSed(listing.getErrorMessages().castToCollection());
    }

    private boolean isAnyMessageAboutSed(Collection<String> messages) {
        return messages.stream()
            .map(msg -> msg.toUpperCase())
            .filter(upperCaseMsg -> upperCaseMsg.contains("SED")
                    || upperCaseMsg.contains("TASK")
                    || upperCaseMsg.contains("PARTICIPANT"))
            .count() > 0;
    }

    private void printListingErrorsAndWarnings(CertifiedProductSearchDetails listing) {
        LOGGER.info("\tErrors for listing: " + listing.getId());
        if (CollectionUtils.isEmpty(listing.getErrorMessages().castToCollection())) {
            LOGGER.info("\t0 errors.");
        } else {
            listing.getErrorMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }

        LOGGER.info("\tWarnings for listing: " + listing.getId());
        if (CollectionUtils.isEmpty(listing.getWarningMessages().castToCollection())) {
            LOGGER.info("\t0 warnings.");
        } else {
            listing.getWarningMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long id) {
        CertifiedProductSearchDetails cp = null;
        try {
            cp = certifiedProductDetailsManager.getCertifiedProductDetails(id);
            LOGGER.debug("Completed retrieval of listing [" + cp.getChplProductNumber() + "]");
        } catch (Exception e) {
            LOGGER.error("Could not retrieve listing [" + id + "] - " + e.getMessage(), e);
        }
        return cp;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    private static final class ListingUpdateResult {
        private CertifiedProductSearchDetails originalListing;
        private CertifiedProductSearchDetails updatedListing;
    }
}