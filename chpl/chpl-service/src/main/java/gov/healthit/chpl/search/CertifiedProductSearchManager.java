package gov.healthit.chpl.search;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResultLegacy;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResultLegacy;
import gov.healthit.chpl.domain.search.SearchRequestLegacy;
import gov.healthit.chpl.domain.search.SearchResponseLegacy;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Deprecated
public class CertifiedProductSearchManager {
    private static final String CERT_STATUS_EVENT_DATE_FORMAT = "yyyy-MM-dd";
    private CertifiedProductSearchDAO searchDao;
    private ListingSearchManager searchManager;
    private DirectReviewSearchService drService;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public CertifiedProductSearchManager(CertifiedProductSearchDAO searchDao,
            ListingSearchManager searchManager,
            DirectReviewSearchService drService) {
        this.searchDao = searchDao;
        this.searchManager = searchManager;
        this.drService = drService;
        this.dateFormatter = DateTimeFormatter.ofPattern(CERT_STATUS_EVENT_DATE_FORMAT);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.COLLECTIONS_LISTINGS, key = "'legacyListings'")
    @Deprecated
    public List<CertifiedProductFlatSearchResultLegacy> searchLegacy() {
        List<CertifiedProductFlatSearchResultLegacy> results = searchDao.getFlatCertifiedProductsLegacy();
        return results;
    }

    @Transactional
    @Deprecated
    public SearchResponseLegacy search(SearchRequestLegacy searchRequest) {

        Collection<CertifiedProductBasicSearchResultLegacy> searchResults = searchDao.search(searchRequest);
        int totalCountSearchResults = searchDao.getTotalResultCount(searchRequest);

        SearchResponseLegacy response = new SearchResponseLegacy(Integer.valueOf(totalCountSearchResults),
                searchResults, searchRequest.getPageSize(), searchRequest.getPageNumber());
        return response;
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

    @Deprecated
    public List<CertifiedProductBasicSearchResult> getBasicListingCollection() {
        //below call should be cached
        List<ListingSearchResult> searchResults = searchManager.getAllListings();

        //convert from the cached objects to the Basic search result
        List<CertifiedProductBasicSearchResult> results = new ArrayList<CertifiedProductBasicSearchResult>(searchResults.size());
        searchResults.stream()
            .map(listingSearchResult -> convertToBasicSearchResult(listingSearchResult))
            .toList();
        return results;
    }


    private CertifiedProductBasicSearchResult convertToBasicSearchResult(ListingSearchResult searchResult) {
        return CertifiedProductBasicSearchResult.builder()
                .id(searchResult.getId())
                .chplProductNumber(searchResult.getChplProductNumber())
                .edition(searchResult.getEdition().getYear())
                .curesUpdate(searchResult.getCuresUpdate())
                .acb(searchResult.getCertificationBody().getName())
                .acbCertificationId(searchResult.getAcbCertificationId())
                .practiceType(searchResult.getPracticeType().getName())
                .developerId(searchResult.getDeveloper().getId())
                .developer(searchResult.getDeveloper().getName())
                .developerStatus(searchResult.getDeveloper().getStatus().getName())
                .product(searchResult.getProduct().getName())
                .version(searchResult.getVersion().getName())
                .promotingInteroperabilityUserCount(searchResult.getPromotingInteroperability().getUserCount())
                .promotingInteroperabilityUserDate(searchResult.getPromotingInteroperability().getUserDate())
                .numMeaningfulUse(searchResult.getPromotingInteroperability().getUserCount())
                .numMeaningfulUseDate(DateUtil.toEpochMillis(searchResult.getPromotingInteroperability().getUserDate()))
                .decertificationDate(searchResult.getDecertificationDate() == null ? null : DateUtil.toEpochMillis(searchResult.getDecertificationDate()))
                .certificationDate(DateUtil.toEpochMillis(searchResult.getCertificationDate()))
                .certificationStatus(searchResult.getCertificationStatus().getName())
                .transparencyAttestationUrl(searchResult.getMandatoryDisclosures())
                .mandatoryDisclosures(searchResult.getMandatoryDisclosures())
                .apiDocumentation(
                        searchResult.getApiDocumentation().stream()
                        .map(obj -> obj.getCriterion().getId() + CertifiedProductSearchResult.FROWNEY_SPLIT_CHAR + obj.getValue())
                        .collect(Collectors.toSet()))
                .serviceBaseUrlList(
                        Stream.of(
                                searchResult.getServiceBaseUrl().getCriterion().getId() + CertifiedProductSearchResult.FROWNEY_SPLIT_CHAR
                                + searchResult.getServiceBaseUrl().getValue())
                        .collect(Collectors.toSet()))
                .surveillanceCount(searchResult.getSurveillanceCount())
                .openSurveillanceCount(searchResult.getOpenSurveillanceCount())
                .closedSurveillanceCount(searchResult.getClosedSurveillanceCount())
                .openSurveillanceNonConformityCount(searchResult.getOpenSurveillanceNonConformityCount())
                .closedSurveillanceNonConformityCount(searchResult.getClosedSurveillanceNonConformityCount())
                .rwtPlansUrl(searchResult.getRwtPlansUrl())
                .rwtResultsUrl(searchResult.getRwtResultsUrl())
                .surveillanceDates(searchResult.getSurveillanceDateRanges().stream()
                        .map(dateRange -> DateUtil.toEpochMillis(dateRange.getStart())
                                + "&"
                                + (dateRange.getEnd() == null ? "" : DateUtil.toEpochMillis(dateRange.getEnd())))
                        .collect(Collectors.toSet()))
                .statusEvents(searchResult.getStatusEvents().stream()
                        .map(statusEvent -> DateUtil.format(statusEvent.getStatusBegin()) + ":" + statusEvent.getStatus().getName())
                        .collect(Collectors.toSet()))
                .criteriaMet(searchResult.getCriteriaMet().stream()
                        .map(criterion -> criterion.getId())
                        .collect(Collectors.toSet()))
                .cqmsMet(searchResult.getCqmsMet().stream()
                        .map(cqm -> cqm.getNumber())
                        .collect(Collectors.toSet()))
                .previousDevelopers(searchResult.getPreviousDevelopers().stream()
                        .map(prevDev -> prevDev.getName())
                        .collect(Collectors.toSet()))
                .build();
    }

    private void populateDirectReviews(CertifiedProductFlatSearchResult searchResult) {
        List<CertificationStatusEvent> statusEvents = createStatusEventsFromFlatSearchResult(searchResult);
        populateDirectReviewFields(searchResult, statusEvents);
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
}
