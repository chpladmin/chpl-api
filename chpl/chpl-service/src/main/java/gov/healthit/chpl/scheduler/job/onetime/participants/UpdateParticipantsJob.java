package gov.healthit.chpl.scheduler.job.onetime.participants;

import java.util.Collection;
import java.util.Iterator;
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

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationCriterion;
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
@Log4j2(topic = "updateParticipantsJobLogger")
public class UpdateParticipantsJob implements Job {
    private static final String FAILURE_TO_UPDATE_MSG = "Listing %s participants could not be updated by any strategy.";
    private static final long FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD = 10912;
    private static final long LAST_LISTING_ID_CONFIRMED_WITH_SED_ISSUE = 11208;

    @Autowired
    private ReprocessFromUploadedCsvStrategy reprocessFromUploadStrategy;

    @Autowired
    private DeduplicationStrategy deduplicationStrategy;

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
    private ParticipantReplacementDao participantReplacementDao;

    private List<ParticipantUpdateStrategy> participantUpdateStrategies;
    private CertificationCriterion g3 = null;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Update Participants job. *********");
        try {
            participantUpdateStrategies = Stream.of(deduplicationStrategy,
                    reprocessFromUploadStrategy).toList();
            g3 = criteriaService.get(Criteria2015.G_3);

            List<ListingSearchResult> activeListingsWithG3ConfirmedWithFlexibleUpload = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationCriteriaIds(Stream.of(g3.getId()).collect(Collectors.toSet()))
                    .certificationCriteriaOperator(SearchSetOperator.AND)
                    .build()).stream()
                    .filter(listingSearchResult -> listingSearchResult.getId() >= FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD
                                                    && listingSearchResult.getId() <= LAST_LISTING_ID_CONFIRMED_WITH_SED_ISSUE)
                    .toList();

            LOGGER.info("Found " + activeListingsWithG3ConfirmedWithFlexibleUpload.size() + " listing uploads attesting to 170.315 (g)(3).");

            activeListingsWithG3ConfirmedWithFlexibleUpload.stream()
                .forEach(listing -> attemptToCorrectSed(listing));

        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }
        LOGGER.info("********* Completed the Update Participants job. *********");
    }

    private void attemptToCorrectSed(ListingSearchResult listing) {
        CertifiedProductSearchDetails listingDetails = getCertifiedProductSearchDetails(listing.getId());
        ListingUpdateResult listingUpdateResult = applyStrategies(listingDetails);

        if (listingUpdateResult.getUpdatedListing() != null) {
            listingValidator.validate(listingUpdateResult.getOriginalListing(), listingUpdateResult.getUpdatedListing());
            if (hasSedErrors(listingUpdateResult.getUpdatedListing())) {
                LOGGER.warn("\tThe updated listing has SED-related errors or warnings so we will not save SED changes: ");
                printListingErrorsAndWarnings(listingUpdateResult.getUpdatedListing());
                LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
            } else {
                //this replacement should execute as a single transaction in case any part of it fails
                try {
                    participantReplacementDao.replaceParticipants(listingUpdateResult.getUpdatedListing());
                } catch (Exception ex) {
                    LOGGER.error("Error replacing participants for listing " + listingUpdateResult.getUpdatedListing().getId(), ex);
                    LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
                } finally {
                    sharedStoreProvider.remove(listingUpdateResult.getUpdatedListing().getId());
                }
            }
        } else {
            LOGGER.error(String.format(FAILURE_TO_UPDATE_MSG, listingUpdateResult.getOriginalListing().getId()));
        }
    }

    private ListingUpdateResult applyStrategies(CertifiedProductSearchDetails listing) {
        ListingUpdateResult result = new ListingUpdateResult();
        //get the details again for the "original" listing because the strategies will change the listing reference
        result.setOriginalListing(getCertifiedProductSearchDetails(listing.getId()));
        Iterator<ParticipantUpdateStrategy> strategyIter = participantUpdateStrategies.iterator();
        while (strategyIter.hasNext() && result.getUpdatedListing() == null) {
            ParticipantUpdateStrategy strategy = strategyIter.next();
            LOGGER.info("Trying strategy " + strategy.getClass().getName() + " for listing " + listing.getId());
            if (strategy.updateParticipants(listing)) {
                result.setUpdatedListing(listing);
                LOGGER.info("Listing " + listing.getId() + " was updated with strategy " + strategy.getClass().getName());
            } else if (strategyIter.hasNext()) {
                //If the current strategy updates SOME of the listing SED data but not all of it -
                //then our "listing" reference may have some changed data
                //but we want to start the next strategy with a fresh non-modified version listing
                listing = getCertifiedProductSearchDetails(listing.getId());
            }
        }
        return result;
    }

    private boolean hasSedErrors(CertifiedProductSearchDetails listing) {
        //there are probably errors and warnings that weren't here originally -
        //some criteria have been removed, we changed measure parsing, etc
        return !CollectionUtils.isEmpty(listing.getErrorMessages())
                && isAnyMessageAboutSed(listing.getErrorMessages());
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
        if (CollectionUtils.isEmpty(listing.getErrorMessages())) {
            LOGGER.info("\t0 errors.");
        } else {
            listing.getErrorMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }

        LOGGER.info("\tWarnings for listing: " + listing.getId());
        if (CollectionUtils.isEmpty(listing.getWarningMessages())) {
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
            LOGGER.info("Completed retrieval of listing [" + cp.getChplProductNumber() + "]");
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
