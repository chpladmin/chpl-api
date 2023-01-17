package gov.healthit.chpl.compliance.directreview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;
import gov.healthit.chpl.util.NullSafeEvaluator;
import one.util.streamex.StreamEx;

@Component
public class DirectReviewListingSharedStoreHandler {
    private SharedListingStoreProvider sharedListingStoreProvider;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private ListingSearchService listingSearchService;
    private DeveloperDAO developerDAO;

    @Autowired
    public DirectReviewListingSharedStoreHandler(@Lazy SharedListingStoreProvider sharedListingStoreProvider,
            @Lazy CertifiedProductDetailsManager certifiedProductDetailsManager, @Lazy ListingSearchService listingSearchService,
            DeveloperDAO developerDAO) {
        this.sharedListingStoreProvider = sharedListingStoreProvider;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.listingSearchService = listingSearchService;
        this.developerDAO = developerDAO;
    }

    public void handler(List<DirectReview> allDirectReviews, Logger logger) {
        logger.info("Clearing shared store listings where direct review has been added or updated.");
        getUniqueDevelopers(allDirectReviews, logger).stream()
                .forEach(dev -> getListingDataForDeveloper(dev, logger).stream()
                        .forEach(listing -> removeListingFromSharedStoreIfDirectReviewUpdated(listing, allDirectReviews, logger)));
    }

    private List<Developer> getUniqueDevelopers(List<DirectReview> allDirectReviews, Logger logger) {
        return StreamEx.of(allDirectReviews)
                .map(dr -> getDeveloper(dr.getDeveloperId(), logger))
                .filter(dev -> dev != null)
                .distinct(dev -> dev.getId())
                .toList();
    }

    private void removeListingFromSharedStoreIfDirectReviewUpdated(CertifiedProductSearchDetails listing, List<DirectReview> allDirectReviews, Logger logger) {
        if (listing.getDirectReviews() == null || listing.getDirectReviews().size() == 0) {
            logger.info("Removing Listing Id {} from the Shared Store", listing.getId());
            sharedListingStoreProvider.remove(listing.getId());
        } else {

            listing.getDirectReviews().parallelStream()
                .forEach(dr -> {
                    if (hasDirectReviewBeenUpdated(findDirectReview(allDirectReviews, dr.getJiraKey()), dr)) {
                        logger.info("Removing Listing Id {} from the Shared Store", listing.getId());
                        sharedListingStoreProvider.remove(listing.getId());
                    } else {
                        logger.info("Not Removing Listing Id {} from the Shared Store - Direct Review Last Updated Date not changed", listing.getId());
                    }
                });
        }
    }

    private Boolean hasDirectReviewBeenUpdated(DirectReview newVersion, DirectReview origVersion) {
        return !NullSafeEvaluator.eval(() -> newVersion.getLastUpdated(), new Date(Long.MIN_VALUE)).equals(
                NullSafeEvaluator.eval(() -> origVersion.getLastUpdated(), new Date(Long.MIN_VALUE)));

    }

    private DirectReview findDirectReview(List<DirectReview> directReviews, String jiraKey) {
        return directReviews.stream()
                .filter(dr -> dr.getJiraKey().equals(jiraKey))
                .findAny()
                .orElse(null);
    }

    private List<CertifiedProductSearchDetails> getListingDataForDeveloper(Developer developer, Logger logger) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developerId(developer.getId())
                    .pageSize(SearchRequest.MAX_PAGE_SIZE)
                    .build();

            return listingSearchService.getAllPagesOfSearchResults(searchRequest).parallelStream()
                    .map(searchResult ->  getListingIfInSharedStore(searchResult.getId(), logger))
                    .filter(listing -> listing != null)
                    .peek(l -> logger.info("Developer {} - Listing {}", developer.getId(), l.getId()))
                    .toList();
        } catch (Exception e) {
            logger.error("Could not retrieve listings for developer: {} - {}", developer.getId(), e.getMessage());
            return new ArrayList<CertifiedProductSearchDetails>();
        }
    }

    private CertifiedProductSearchDetails getListingIfInSharedStore(Long listingId, Logger logger) {
        try {
            if (sharedListingStoreProvider.containsKey(listingId)) {
                return certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
            } else {
                logger.info("Listing Id: {} is not currently in the Shared Store", listingId);
                return null;
            }
        } catch (EntityRetrievalException e) {
            logger.error("Could not lookup Listing: {}", listingId, e);
            return null;
        }
    }

    private Developer getDeveloper(Long developerId, Logger logger) {
        try {
            return developerDAO.getById(developerId);
        } catch (Exception e) {
            logger.error("Could not find developer: {}", developerId);
            return null;
        }
    }
 }
