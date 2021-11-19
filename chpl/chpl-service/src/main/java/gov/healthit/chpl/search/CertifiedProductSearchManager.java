package gov.healthit.chpl.search;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
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
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResultLegacy;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResultLegacy;
import gov.healthit.chpl.domain.search.SearchRequestLegacy;
import gov.healthit.chpl.domain.search.SearchResponseLegacy;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.service.RealWorldTestingEligibility;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingEligiblityCachingService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertifiedProductSearchManager {
    private static final String CERT_STATUS_EVENT_DATE_FORMAT = "yyyy-MM-dd";
    private CertifiedProductSearchDAO searchDao;
    private RealWorldTestingEligiblityCachingService rwtService;
    private DirectReviewSearchService drService;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public CertifiedProductSearchManager(CertifiedProductSearchDAO searchDao,
            RealWorldTestingEligiblityCachingService rwtService,
            DirectReviewSearchService drService) {
        this.searchDao = searchDao;
        this.rwtService = rwtService;
        this.drService = drService;
        this.dateFormatter = DateTimeFormatter.ofPattern(CERT_STATUS_EVENT_DATE_FORMAT);
    }

    @Deprecated
    @Cacheable(value = CacheNames.COLLECTIONS_LISTINGS, key = "'listings'")
    public List<CertifiedProductFlatSearchResult> getFlatListingCollection() {
        List<CertifiedProductFlatSearchResult> results = searchDao.getFlatCertifiedProducts();
        LOGGER.info("Populating Direct Review fields for collections");
        Date start = new Date();
        results.parallelStream()
            .forEach(searchResult -> populateDirectReviews(searchResult));
        Date end = new Date();
        LOGGER.info("Completed Populating Direct Review fields  for collections [ " + (end.getTime() - start.getTime()) + " ms ]");
        return results;
    }

    @Cacheable(value = CacheNames.COLLECTIONS_SEARCH)
    public List<CertifiedProductBasicSearchResult> getSearchListingCollection() {
        List<CertifiedProductBasicSearchResult> results = searchDao.getCertifiedProducts();
        LOGGER.info("Populating Direct Review fields for search");
        Date start = new Date();
        results.parallelStream()
            .forEach(searchResult -> populateDirectReviews(searchResult));
        Date end = new Date();
        LOGGER.info("Completed Populating Direct Review fields  for search [ " + (end.getTime() - start.getTime()) + " ms ]");
        LOGGER.info("Populating RWT Eligibility field for search");
        start = new Date();
        results.parallelStream()
            .filter(listing -> listing.getEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()))
            .forEach(listing -> populateRwtEligibility(listing));
        end = new Date();
        LOGGER.info("Completed Populating RWT Eligibility field for search [ " + (end.getTime() - start.getTime()) + " ms ]");
        LOGGER.info("Populating RWT Eligibility field for 2011/2014 listings for search");
        start = new Date();
        results.parallelStream()
            .filter(listing -> !listing.getEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()))
            .forEach(listing -> listing.setIsRwtEligible(false));
        end = new Date();
        LOGGER.info("Completed Populating RWT Eligibility field for 2011/2014 listings for search [ " + (end.getTime() - start.getTime()) + " ms ]");
        return results;
    }

    private void populateRwtEligibility(CertifiedProductBasicSearchResult searchResult) {
        RealWorldTestingEligibility rwtElig = rwtService.getRwtEligibility(searchResult.getId());
        searchResult.setIsRwtEligible(rwtElig.getEligibilityYear().isPresent());
    }

    private void populateDirectReviews(CertifiedProductBasicSearchResult searchResult) {
        List<CertificationStatusEvent> statusEvents = createStatusEventsFromBasicSearchResult(searchResult);
        populateDirectReviewFields(searchResult, statusEvents);
    }

    private void populateDirectReviews(CertifiedProductFlatSearchResult searchResult) {
        List<CertificationStatusEvent> statusEvents = createStatusEventsFromFlatSearchResult(searchResult);
        populateDirectReviewFields(searchResult, statusEvents);
    }

    private List<CertificationStatusEvent> createStatusEventsFromBasicSearchResult(CertifiedProductBasicSearchResult listing) {
        return listing.getStatusEvents().stream()
            .map(dateAndStatusStr -> convertToCertificationStatusEvent(dateAndStatusStr))
            .collect(Collectors.toList());
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

    private void populateDirectReviewFields(CertifiedProductSearchResult searchResult, List<CertificationStatusEvent> statusEvents) {
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
        List<CertifiedProductFlatSearchResultLegacy> results = searchDao.getFlatCertifiedProductsLegacy();
        return results;
    }

    @Transactional
    public SearchResponseLegacy search(SearchRequestLegacy searchRequest) {

        Collection<CertifiedProductBasicSearchResultLegacy> searchResults = searchDao.search(searchRequest);
        int totalCountSearchResults = searchDao.getTotalResultCount(searchRequest);

        SearchResponseLegacy response = new SearchResponseLegacy(Integer.valueOf(totalCountSearchResults),
                searchResults, searchRequest.getPageSize(), searchRequest.getPageNumber());
        return response;
    }
}
