package gov.healthit.chpl.manager;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResultLegacy;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.service.DirectReviewSearchService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertifiedProductSearchManager {
    private static final String CERT_STATUS_EVENT_DATE_FORMAT = "yyyy-MM-dd";
    private CertifiedProductSearchDAO searchDao;
    private DirectReviewSearchService drService;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public CertifiedProductSearchManager(CertifiedProductSearchDAO searchDao, DirectReviewSearchService drService) {
        this.searchDao = searchDao;
        this.drService = drService;
        this.dateFormatter = DateTimeFormatter.ofPattern(CERT_STATUS_EVENT_DATE_FORMAT);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.COLLECTIONS_LISTINGS, key = "'listings'")
    public List<CertifiedProductFlatSearchResult> search() {
        List<CertifiedProductFlatSearchResult> results = searchDao.getAllCertifiedProducts();
        results.stream()
            .forEach(searchResult -> populateDirectReviewFields(searchResult));
        return results;
    }

    private void populateDirectReviewFields(CertifiedProductFlatSearchResult searchResult) {
        List<CertificationStatusEvent> statusEvents = createStatusEventsFromFlatSearchResult(searchResult);
        List<DirectReview> listingDrs = drService.getDirectReviewsRelatedToListing(searchResult.getId(),
                searchResult.getDeveloperId(),
                searchResult.getEdition(),
                statusEvents);
        searchResult.setDirectReviewCount(listingDrs.size());
        searchResult.setOpenDirectReviewNonConformityCount(
                calculateNonConformitiesWithStatusForListing(listingDrs, searchResult.getId(), DirectReviewNonConformity.STATUS_OPEN));
        searchResult.setClosedDirectReviewNonConformityCount(
                calculateNonConformitiesWithStatusForListing(listingDrs, searchResult.getId(), DirectReviewNonConformity.STATUS_CLOSED));
    }

    private List<CertificationStatusEvent> createStatusEventsFromFlatSearchResult(CertifiedProductFlatSearchResult listing) {
        return Stream.of(listing.getStatusEvents().split("&"))
            .map(dateAndStatusStr -> convertToCertificationStatusEvent(dateAndStatusStr))
            .collect(Collectors.toList());
    }

    private CertificationStatusEvent convertToCertificationStatusEvent(String dateAndStatusStr) {
        if (StringUtils.isEmpty(dateAndStatusStr)) {
            return null;
        }
        //2010-12-28:Active&2016-04-01:Retired
        String[] splitDateAndStatus = dateAndStatusStr.split(":");
        if (splitDateAndStatus == null || splitDateAndStatus.length != 2) {
            LOGGER.warn("Unexpected format of status events data: " + dateAndStatusStr);
            return null;
        }
        String statusName = splitDateAndStatus[1];
        Long statusDate = -1L;
        try {
            LocalDate statusDateTime = LocalDate.parse(splitDateAndStatus[0], dateFormatter);
            statusDate = statusDateTime != null ? statusDateTime.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() : -1L;
        } catch (Exception ex) {
            LOGGER.warn("Unable to convert " + splitDateAndStatus[0] + " to milli value", ex);
        }
        return CertificationStatusEvent.builder()
            .status(CertificationStatus.builder()
                    .name(statusName)
                    .build())
            .eventDate(statusDate)
            .build();
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

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.COLLECTIONS_LISTINGS, key = "'legacyListings'")
    @Deprecated
    public List<CertifiedProductFlatSearchResultLegacy> searchLegacy() {
        List<CertifiedProductFlatSearchResultLegacy> results = searchDao.getAllCertifiedProductsLegacy();
        return results;
    }

    @Transactional
    public SearchResponse search(SearchRequest searchRequest) {

        Collection<CertifiedProductBasicSearchResult> searchResults = searchDao.search(searchRequest);
        int totalCountSearchResults = searchDao.getTotalResultCount(searchRequest);

        SearchResponse response = new SearchResponse(Integer.valueOf(totalCountSearchResults),
                searchResults, searchRequest.getPageSize(), searchRequest.getPageNumber());
        return response;
    }
}
