package gov.healthit.chpl.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.caching.ListingSearchCacheable;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.search.dao.ListingSearchDao;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.StatusEventSearchResult;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingSearchManager {
    private ListingSearchDao searchDao;
    private DirectReviewSearchService drService;

    @Autowired
    public ListingSearchManager(ListingSearchDao searchDao,
            DirectReviewSearchService drService) {
        this.searchDao = searchDao;
        this.drService = drService;
    }

    @ListingSearchCacheable
    public List<ListingSearchResult> getAllListings() {
        return getAllListingsNoCache();
    }

    public  List<ListingSearchResult> getAllListingsNoCache() {
        List<ListingSearchResult> results = searchDao.getListingSearchResults();
        results.parallelStream()
                .forEach(searchResult -> populateDirectReviews(searchResult));
        return results;
    }

    private void populateDirectReviews(ListingSearchResult searchResult) {
        List<CertificationStatusEvent> statusEvents = createStatusEventsFromSearchResult(searchResult);
        populateDirectReviewFields(searchResult, statusEvents);
    }

    private List<CertificationStatusEvent> createStatusEventsFromSearchResult(ListingSearchResult searchResult) {
        return searchResult.getStatusEvents().stream()
            .map(statusEvent -> convertToCertificationStatusEvent(statusEvent))
            .collect(Collectors.toList());
    }

    private CertificationStatusEvent convertToCertificationStatusEvent(StatusEventSearchResult statusEvent) {
        return CertificationStatusEvent.builder()
            .status(CertificationStatus.builder()
                    .name(statusEvent.getStatus().getName())
                    .build())
            .eventDate(DateUtil.toEpochMillis(statusEvent.getStatusStart()))
            .build();
    }

    private void populateDirectReviewFields(ListingSearchResult searchResult, List<CertificationStatusEvent> statusEvents) {
        List<DirectReview> listingDrs = drService.getDirectReviewsRelatedToListing(searchResult.getId(),
                searchResult.getDeveloper().getId(),
                searchResult.getEdition() != null ? searchResult.getEdition().getName() : null,
                statusEvents, LOGGER);
        searchResult.setDirectReviewCount(listingDrs.size());
        searchResult.setOpenDirectReviewNonConformityCount(
                calculateNonConformitiesWithStatusForListing(listingDrs, searchResult.getId(), DirectReviewNonConformity.STATUS_OPEN));
        searchResult.setClosedDirectReviewNonConformityCount(
                calculateNonConformitiesWithStatusForListing(listingDrs, searchResult.getId(), DirectReviewNonConformity.STATUS_CLOSED));
    }

    private int calculateNonConformitiesWithStatusForListing(List<DirectReview> listingDrs, Long listingId,
            String nonConformityStatus) {
        return (int) listingDrs.stream()
        .filter(dr -> dr.getNonConformities() != null && dr.getNonConformities().size() > 0)
        .flatMap(dr -> dr.getNonConformities().stream())
        .filter(nc -> isNonConformityRelatedToListing(nc, listingId))
        .filter(nc -> nc.getNonConformityStatus() != null
                && nc.getNonConformityStatus().equalsIgnoreCase(nonConformityStatus))
        .count();
    }

    private boolean isNonConformityRelatedToListing(DirectReviewNonConformity nonConformity, Long listingId) {
        if (nonConformity.getDeveloperAssociatedListings() == null
                || nonConformity.getDeveloperAssociatedListings().size() == 0) {
            return true;
        }
        return nonConformity.getDeveloperAssociatedListings().stream()
                .filter(dal -> dal.getId().equals(listingId))
                .findAny().isPresent();
    }
}
