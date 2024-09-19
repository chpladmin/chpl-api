package gov.healthit.chpl.scheduler.job.onetime.sed;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    private static final long FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD = 10912;

    @Autowired
    private ReprocessFromUploadedCsvHelper reprocessFromUploadHelper;

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
        LOGGER.info("********* Starting the Update SED Friendly Ids job. *********");
        try {
            g3 = criteriaService.get(Criteria2015.G_3);

            List<ListingSearchResult> listingsWithG3ConfirmedWithFlexibleUpload = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationCriteriaIds(Stream.of(g3.getId()).collect(Collectors.toSet()))
                    .certificationCriteriaOperator(SearchSetOperator.AND)
                    .build()).stream()
                    //filter out listings added before we started saving the upload files
                    .filter(listingSearchResult -> listingSearchResult.getId() >= FIRST_LISTING_ID_CONFIRMED_WITH_FLEXIBLE_UPLOAD)
                    //filter out the two listings we know have person names in data
                    .filter(listingSearchResult -> !listingSearchResult.getId().equals(11213L) && !listingSearchResult.getId().equals(11232L))
                    .toList();

            LOGGER.info("Found " + listingsWithG3ConfirmedWithFlexibleUpload.size() + " listing uploads attesting to 170.315 (g)(3).");

            listingsWithG3ConfirmedWithFlexibleUpload.stream()
                .forEach(listing -> attemptToSaveFriendlyIds(listing));

        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }

        LOGGER.info("*** REPORT OF LISTINGS WITH ANY MISSING FRIENDLY IDS: ***");
        LOGGER.info("\tDatabase ID\tCHPL Product Number\tDeveloper\tACB\tStatus\tTasks Missing ID?\tParticipants Missing ID?");
        try {
            List<ListingSearchResult> listingsWithG3 = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                .certificationCriteriaIds(Stream.of(g3.getId()).collect(Collectors.toSet()))
                .certificationCriteriaOperator(SearchSetOperator.AND)
                .build()).stream()
                .toList();
            listingsWithG3.stream()
                .forEach(listingSearchResult -> {
                    try {
                        CertifiedProductSearchDetails currListing = certifiedProductDetailsManager.getCertifiedProductDetails(listingSearchResult.getId());
                        boolean hasTestTasksWithMissingFriendlyId = currListing.getSed().getTestTasks().stream()
                            .filter(tt -> StringUtils.isEmpty(tt.getFriendlyId()))
                            .findAny().isPresent();
                        boolean hasParticipantsWithMissingFriendlyId = currListing.getSed().getTestTasks().stream()
                                .flatMap(tt -> tt.getTestParticipants().stream())
                                .filter(tp -> StringUtils.isEmpty(tp.getFriendlyId()))
                                .findAny().isPresent();
                        if (hasTestTasksWithMissingFriendlyId
                                || hasParticipantsWithMissingFriendlyId) {
                            LOGGER.info("\t" + currListing.getId()
                                    + "\t" + currListing.getChplProductNumber()
                                    + "\t" + currListing.getDeveloper().getName()
                                    + "\t" + currListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString()
                                    + "\t" + currListing.getCurrentStatus().getStatus().getName()
                                    + "\t" + (hasTestTasksWithMissingFriendlyId ? "Y" : "N")
                                    + "\t" + (hasParticipantsWithMissingFriendlyId ? "Y" : "N"));
                        }
                    } catch (Exception ex) {
                        LOGGER.warn("Unable to get listing details for " + listingSearchResult.getId() + ". That will not be included in the output.");
                    }

            });
        } catch (Exception ex) {
            LOGGER.error("Unable to create output about listings still needing update.");
        }

        LOGGER.info("********* Completed the Update SED Friendly IDs job. *********");
    }

    private void attemptToSaveFriendlyIds(ListingSearchResult listing) {
        LOGGER.info("Processing listing ID " + listing.getId());
        CertifiedProductSearchDetails listingDetails = getCertifiedProductSearchDetails(listing.getId());
        ListingUpdateResult listingUpdateResult = updateFriendlyIds(listingDetails);

        if (listingUpdateResult.getUpdatedListing() != null) {
            listingValidator.validate(listingUpdateResult.getOriginalListing(), listingUpdateResult.getUpdatedListing());
            if (hasSedErrors(listingUpdateResult.getUpdatedListing())) {
                LOGGER.warn("\tThe updated listing has SED-related errors or warnings so we will not save SED changes: ");
                printListingErrorsAndWarnings(listingUpdateResult.getUpdatedListing());
                LOGGER.warn("Listing " + listing.getId() + " friendly IDs could not be updated.");
            } else {
                //this replacement should execute as a single transaction in case any part of it fails
                try {
                    sedFriendlyIdReplacementDao.updateSedFriendlyIds(listingUpdateResult.getUpdatedListing());
                    LOGGER.info("Completed updating SED friendly IDs for listing " + listingUpdateResult.getUpdatedListing().getId());
                } catch (Exception ex) {
                    LOGGER.error("Error updating SED friendly IDs for listing " + listingUpdateResult.getUpdatedListing().getId(), ex);
                    LOGGER.error("Listing " + listing.getId() + " friendly IDs could not be updated.");
                } finally {
                    sharedStoreProvider.remove(listingUpdateResult.getUpdatedListing().getId());
                }
            }
        } else {
            LOGGER.warn("Listing " + listing.getId() + " friendly IDs could not be updated.");
        }
    }

    private ListingUpdateResult updateFriendlyIds(CertifiedProductSearchDetails listing) {
        ListingUpdateResult listingUpdateResult = new ListingUpdateResult();
        listingUpdateResult.setOriginalListing(listing);

        LOGGER.info("Updating test task friendly IDs for listing ID " + listing.getId());
        if (reprocessFromUploadHelper.updateTasks(listing)) {
            listingUpdateResult.setUpdatedListing(listing);
            LOGGER.info("Tasks for listing " + listing.getId() + " were updated.");
        }
        LOGGER.info("Updating test participant friendly IDs for listing ID " + listing.getId());
        if (reprocessFromUploadHelper.updateParticipants(listing)) {
            listingUpdateResult.setUpdatedListing(listing);
            LOGGER.info("Participants for listing " + listing.getId() + " were updated.");
        }
        return listingUpdateResult;
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