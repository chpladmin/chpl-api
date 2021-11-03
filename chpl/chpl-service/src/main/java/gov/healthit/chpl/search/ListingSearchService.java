package gov.healthit.chpl.search;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchResponse;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.DirectReviewSearchService;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("listingSearchService")
@NoArgsConstructor
@Log4j2
public class ListingSearchService {
    private static final String CURES_UPDATE_EDITION = "2015 Cures Update";
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private CertifiedProductSearchManager cpSearchManager;
    private DirectReviewSearchService drService;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ListingSearchService(SearchRequestValidator searchRequestValidator,
            CertifiedProductSearchManager cpSearchManager,
            DirectReviewSearchService drService) {
        this.searchRequestValidator = searchRequestValidator;
        this.cpSearchManager = cpSearchManager;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        this.drService = drService;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
    }

    public SearchResponse search(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<CertifiedProductBasicSearchResult> listings = cpSearchManager.getSearchListingCollection();
        LOGGER.debug("Total listings: " + listings.size());
        List<CertifiedProductBasicSearchResult> filteredListings = listings.stream()
            .filter(listing -> matchesSearchTerm(listing, searchRequest.getSearchTerm()))
            .filter(listing -> matchesAcbNames(listing, searchRequest.getCertificationBodies()))
            .filter(listing -> matchesCertificationStatuses(listing, searchRequest.getCertificationStatuses()))
            .filter(listing -> matchesCertificationEditions(listing, searchRequest.getCertificationEditions()))
            .filter(listing -> matchesDeveloper(listing, searchRequest.getDeveloper()))
            .filter(listing -> matchesProduct(listing, searchRequest.getProduct()))
            .filter(listing -> matchesVersion(listing, searchRequest.getVersion()))
            .filter(listing -> matchesPracticeType(listing, searchRequest.getPracticeType()))
            .filter(listing -> matchesCriteria(listing, searchRequest.getCertificationCriteriaIds(), searchRequest.getCertificationCriteriaOperator()))
            .filter(listing -> matchesCqms(listing, searchRequest.getCqms(), searchRequest.getCqmsOperator()))
            .filter(listing -> matchesCertificationDateRange(listing, searchRequest.getCertificationDateStart(), searchRequest.getCertificationDateEnd()))
            .filter(listing -> matchesComplianceFilter(listing, searchRequest.getComplianceActivity()))
            .filter(listing -> matchesRwtFilter(listing, searchRequest.getRwtOptions(), searchRequest.getRwtOperator()))
            .collect(Collectors.toList());
        LOGGER.debug("Total filtered listings: " + filteredListings.size());

        SearchResponse response = new SearchResponse();
        response.setRecordCount(filteredListings.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());
        response.setDirectReviewsAvailable(drService.getDirectReviewsAvailable());

        sort(filteredListings, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<CertifiedProductBasicSearchResult> pageOfListings
            = getPage(filteredListings, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfListings);
        return response;
    }

    private boolean matchesSearchTerm(CertifiedProductBasicSearchResult listing, String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return true;
        }

        String searchTermUpperCase = searchTerm.toUpperCase();
        return (!StringUtils.isEmpty(listing.getDeveloper()) && listing.getDeveloper().toUpperCase().contains(searchTermUpperCase))
                || (!StringUtils.isEmpty(listing.getProduct()) && listing.getProduct().toUpperCase().contains(searchTermUpperCase))
                || (!StringUtils.isEmpty(listing.getChplProductNumber()) && listing.getChplProductNumber().toUpperCase().contains(searchTermUpperCase))
                || (!StringUtils.isEmpty(listing.getAcbCertificationId()) && listing.getAcbCertificationId().toUpperCase().contains(searchTermUpperCase));
    }

    private boolean matchesAcbNames(CertifiedProductBasicSearchResult listing, Set<String> acbNames) {
        if (acbNames == null || acbNames.size() == 0) {
            return true;
        }

        List<String> acbNamesUpperCase = acbNames.stream().map(acbName -> acbName.toUpperCase()).collect(Collectors.toList());
        return !StringUtils.isEmpty(listing.getAcb()) && acbNamesUpperCase.contains(listing.getAcb().toUpperCase());
    }

    private boolean matchesCertificationStatuses(CertifiedProductBasicSearchResult listing, Set<String> certificationStatuses) {
        if (certificationStatuses == null || certificationStatuses.size() == 0) {
            return true;
        }

        List<String> certificationStatusesUpperCase = certificationStatuses.stream().map(certStatus -> certStatus.toUpperCase()).collect(Collectors.toList());
        return !StringUtils.isEmpty(listing.getCertificationStatus()) && certificationStatusesUpperCase.contains(listing.getCertificationStatus().toUpperCase());
    }

    private boolean matchesCertificationEditions(CertifiedProductBasicSearchResult listing, Set<String> certificationEditions) {
        if (CollectionUtils.isEmpty(certificationEditions)) {
            return true;
        }

        return certificationEditions.stream()
            .anyMatch(edition -> matchesCertificationEdition(listing, edition));
    }

    private boolean matchesCertificationEdition(CertifiedProductBasicSearchResult listing, String certificationEdition) {
        boolean editionMatch = false;
        if (certificationEdition.equals(CURES_UPDATE_EDITION)) {
            editionMatch = !StringUtils.isEmpty(listing.getEdition())
                    && listing.getEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                    && BooleanUtils.isTrue(listing.getCuresUpdate());
        } else if (certificationEdition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            editionMatch = !StringUtils.isEmpty(listing.getEdition())
                    && listing.getEdition().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                    && BooleanUtils.isFalse(listing.getCuresUpdate());
        } else {
            editionMatch = !StringUtils.isEmpty(listing.getEdition())
                    && certificationEdition.equals(listing.getEdition());
        }
        return editionMatch;
    }

    private boolean matchesDeveloper(CertifiedProductBasicSearchResult listing, String developer) {
        if (StringUtils.isEmpty(developer)) {
            return true;
        }

        return !StringUtils.isEmpty(listing.getDeveloper())
                && listing.getDeveloper().toUpperCase().contains(developer.toUpperCase());
    }

    private boolean matchesProduct(CertifiedProductBasicSearchResult listing, String product) {
        if (StringUtils.isEmpty(product)) {
            return true;
        }

        return !StringUtils.isEmpty(listing.getProduct())
                && listing.getProduct().toUpperCase().contains(product.toUpperCase());
    }

    private boolean matchesVersion(CertifiedProductBasicSearchResult listing, String version) {
        if (StringUtils.isEmpty(version)) {
            return true;
        }

        return !StringUtils.isEmpty(listing.getVersion())
                && listing.getVersion().toUpperCase().contains(version.toUpperCase());
    }

    private boolean matchesPracticeType(CertifiedProductBasicSearchResult listing, String practiceType) {
        if (StringUtils.isEmpty(practiceType)) {
            return true;
        }

        return !StringUtils.isEmpty(listing.getPracticeType())
                && listing.getPracticeType().toUpperCase().contains(practiceType.toUpperCase());
    }

    private boolean matchesCriteria(CertifiedProductBasicSearchResult listing, Set<Long> criteriaIds,
            SearchSetOperator searchOperator) {
        if (criteriaIds == null || criteriaIds.size() == 0) {
            return true;
        }
        if (searchOperator.equals(SearchSetOperator.AND)) {
            return criteriaIds.stream()
                .allMatch(criterionId -> listing.getCriteriaMet().contains(criterionId));
        } else if (searchOperator.equals(SearchSetOperator.OR)) {
            return criteriaIds.stream()
                    .anyMatch(criterionId -> listing.getCriteriaMet().contains(criterionId));
        }
        return false;
    }

    private boolean matchesCqms(CertifiedProductBasicSearchResult listing, Set<String> cqmNumbers,
            SearchSetOperator searchOperator) {
        if (cqmNumbers == null || cqmNumbers.size() == 0) {
            return true;
        }
        if (searchOperator.equals(SearchSetOperator.AND)) {
            return cqmNumbers.stream()
                .allMatch(cqmNumber -> listing.getCqmsMet().contains(cqmNumber));
        } else if (searchOperator.equals(SearchSetOperator.OR)) {
            return cqmNumbers.stream()
                    .anyMatch(cqmNumber -> listing.getCqmsMet().contains(cqmNumber));
        }
        return false;
    }

    private boolean matchesComplianceFilter(CertifiedProductBasicSearchResult listing, ComplianceSearchFilter complianceFilter) {
        if (complianceFilter == null
                || (complianceFilter.getHasHadComplianceActivity() == null
                    && CollectionUtils.isEmpty(complianceFilter.getNonConformityOptions()))) {
            return true;
        }

        boolean matchesHasHadComplianceActivityFilter = matchesHasHadComplianceFilter(listing, complianceFilter.getHasHadComplianceActivity());

        Boolean matchesNeverNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.NEVER_NONCONFORMITY)) {
            matchesNeverNonConformityFilter = listing.getOpenSurveillanceNonConformityCount() == 0
                    && listing.getClosedSurveillanceNonConformityCount() == 0
                    && listing.getOpenDirectReviewNonConformityCount() == 0
                    && listing.getClosedDirectReviewNonConformityCount() == 0;
        }
        Boolean matchesOpenNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.OPEN_NONCONFORMITY)) {
            matchesOpenNonConformityFilter = listing.getOpenDirectReviewNonConformityCount() > 0
                    || listing.getOpenSurveillanceNonConformityCount() > 0;
        }
        Boolean matchesClosedNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.CLOSED_NONCONFORMITY)) {
            matchesClosedNonConformityFilter = listing.getClosedDirectReviewNonConformityCount() > 0
                    || listing.getClosedSurveillanceNonConformityCount() > 0;
        }
        Boolean matchesNotNeverNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.NOT_NEVER_NONCONFORMITY)) {
            matchesNotNeverNonConformityFilter = listing.getOpenSurveillanceNonConformityCount() > 0
                    || listing.getClosedSurveillanceNonConformityCount() > 0
                    || listing.getOpenDirectReviewNonConformityCount() > 0
                    || listing.getClosedDirectReviewNonConformityCount() > 0;
        }
        Boolean matchesNotOpenNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.NOT_OPEN_NONCONFORMITY)) {
            matchesNotOpenNonConformityFilter = listing.getOpenDirectReviewNonConformityCount() == 0
                    && listing.getOpenSurveillanceNonConformityCount() == 0;
        }
        Boolean matchesNotClosedNonConformityFilter = null;
        if (complianceFilter.getNonConformityOptions().contains(NonConformitySearchOptions.NOT_CLOSED_NONCONFORMITY)) {
            matchesNotClosedNonConformityFilter = listing.getClosedDirectReviewNonConformityCount() == 0
                    && listing.getClosedSurveillanceNonConformityCount() == 0;
        }

        if (ObjectUtils.anyNotNull(matchesNeverNonConformityFilter, matchesOpenNonConformityFilter, matchesClosedNonConformityFilter,
                matchesNotNeverNonConformityFilter, matchesNotOpenNonConformityFilter, matchesNotClosedNonConformityFilter)) {
            boolean matchesNonConformityFilter = applyOperation(complianceFilter.getNonConformityOptionsOperator(),
                    matchesNeverNonConformityFilter, matchesOpenNonConformityFilter, matchesClosedNonConformityFilter,
                    matchesNotNeverNonConformityFilter, matchesNotOpenNonConformityFilter, matchesNotClosedNonConformityFilter);
            return matchesHasHadComplianceActivityFilter && matchesNonConformityFilter;
        }
        return matchesHasHadComplianceActivityFilter;
    }

    private boolean matchesHasHadComplianceFilter(CertifiedProductBasicSearchResult listing, Boolean hasHadComplianceFilter) {
        if (hasHadComplianceFilter == null) {
            return true;
        }
        if (hasHadComplianceFilter) {
            return listing.getSurveillanceCount() > 0 || listing.getDirectReviewCount() > 0;
        } else {
            return listing.getSurveillanceCount() == 0 && listing.getDirectReviewCount() == 0;
        }
    }

    private boolean matchesRwtFilter(CertifiedProductBasicSearchResult listing, Set<RwtSearchOptions> rwtOptions, SearchSetOperator rwtOperator) {
        if (CollectionUtils.isEmpty(rwtOptions)) {
            return true;
        }

        Boolean matchesIsEligibleFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.IS_ELIGIBLE)) {
            matchesIsEligibleFilter = listing.getIsRwtEligible();
        }
        Boolean matchesNotEligibleFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.NOT_ELIGIBLE)) {
            matchesNotEligibleFilter = BooleanUtils.isFalse(listing.getIsRwtEligible());
        }
        Boolean matchesHasPlansFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.HAS_PLANS_URL)) {
            matchesHasPlansFilter = StringUtils.isNotBlank(listing.getRwtPlansUrl());
        }
        Boolean matchesNoPlansFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.NO_PLANS_URL)) {
            matchesNoPlansFilter = StringUtils.isBlank(listing.getRwtPlansUrl());
        }
        Boolean matchesResultsFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.HAS_RESULTS_URL)) {
            matchesResultsFilter = StringUtils.isNotBlank(listing.getRwtResultsUrl());
        }
        Boolean matchesNoResultsFilter = null;
        if (rwtOptions.contains(RwtSearchOptions.NO_RESULTS_URL)) {
            matchesNoResultsFilter = StringUtils.isBlank(listing.getRwtResultsUrl());
        }

        boolean matchesRwtFilter = applyOperation(rwtOperator, matchesIsEligibleFilter, matchesNotEligibleFilter,
                matchesHasPlansFilter, matchesNoPlansFilter, matchesResultsFilter,
                matchesNoResultsFilter);
        return matchesRwtFilter;
    }

    private boolean applyOperation(SearchSetOperator operation, Boolean... filters) {
        List<Boolean> nonNullFilters = Stream.of(filters)
                .filter(booleanElement -> booleanElement != null)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nonNullFilters)) {
            return false;
        }

        if (operation == null || operation.equals(SearchSetOperator.AND)) {
            return BooleanUtils.and(nonNullFilters.toArray(new Boolean[nonNullFilters.size()]));
        } else if (operation.equals(SearchSetOperator.OR)) {
            return BooleanUtils.or(nonNullFilters.toArray(new Boolean[nonNullFilters.size()]));
        } else {
            LOGGER.error("Unknown operation: " + operation);
        }
        return false;
    }

    private boolean matchesCertificationDateRange(CertifiedProductBasicSearchResult listing, String certificationDateRangeStart,
            String certificationDateRangeEnd) {
        if (StringUtils.isAllEmpty(certificationDateRangeStart, certificationDateRangeEnd)) {
            return true;
        }
        LocalDate startDate = parseLocalDate(certificationDateRangeStart);
        LocalDate endDate = parseLocalDate(certificationDateRangeEnd);
        if (listing.getCertificationDate() != null) {
            LocalDate listingCertificationDate = parseLocalDate(listing.getCertificationDate());
            if (startDate == null && endDate != null) {
                return listingCertificationDate.isEqual(endDate) || listingCertificationDate.isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return listingCertificationDate.isEqual(startDate) || listingCertificationDate.isAfter(startDate);
            } else {
                return listingCertificationDate.isEqual(startDate) || listingCertificationDate.isEqual(endDate)
                        || (listingCertificationDate.isBefore(endDate) && listingCertificationDate.isAfter(startDate));
            }
        }
        return false;
    }

    private LocalDate parseLocalDate(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }

        LocalDate date = null;
        try {
            date = LocalDate.parse(dateString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + dateString + " as date of the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
        }
        return date;
    }

    private LocalDate parseLocalDate(Long millisSinceEpoch) {
        return Instant.ofEpochMilli(millisSinceEpoch).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private List<CertifiedProductBasicSearchResult> getPage(List<CertifiedProductBasicSearchResult> listings, int beginIndex, int endIndex) {
        if (endIndex > listings.size()) {
            endIndex = listings.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<CertifiedProductBasicSearchResult>();
        }
        LOGGER.debug("Getting filtered listing results between [" + beginIndex + ", " + endIndex + ")");
        return listings.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(SearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(SearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<CertifiedProductBasicSearchResult> listings, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case EDITION:
                listings.sort(new EditionComparator(descending));
                break;
            case DEVELOPER:
                listings.sort(new DeveloperComparator(descending));
                break;
            case PRODUCT:
                listings.sort(new ProductComparator(descending));
                break;
            case VERSION:
                listings.sort(new VersionComparator(descending));
                break;
            case CERTIFICATION_DATE:
                listings.sort(new CertificationDateComparator(descending));
                break;
            case CHPL_ID:
                listings.sort(new ChplIdComparator(descending));
                break;
            case STATUS:
                listings.sort(new CertificationStatusComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class EditionComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        EditionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getEdition(), listing2.getEdition())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getEffectiveEdition().compareTo(listing2.getEffectiveEdition())) * sortFactor;
        }
    }

    private class DeveloperComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        DeveloperComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getDeveloper(), listing2.getDeveloper())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getDeveloper().compareTo(listing2.getDeveloper())) * sortFactor;
        }
    }

    private class ProductComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        ProductComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getProduct(), listing2.getProduct())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getProduct().compareTo(listing2.getProduct())) * sortFactor;
        }
    }

    private class VersionComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        VersionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getVersion(), listing2.getVersion())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getVersion().compareTo(listing2.getVersion())) * sortFactor;
        }
    }

    private class CertificationDateComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        CertificationDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (listing1.getCertificationDate() == null ||  listing2.getCertificationDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getCertificationDate().compareTo(listing2.getCertificationDate())) * sortFactor;
        }
    }

    private class ChplIdComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        ChplIdComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getChplProductNumber(), listing2.getChplProductNumber())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getChplProductNumber().compareTo(listing2.getChplProductNumber())) * sortFactor;
        }
    }

    private class CertificationStatusComparator implements Comparator<CertifiedProductBasicSearchResult> {
        private boolean descending = false;

        CertificationStatusComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getCertificationStatus(), listing2.getCertificationStatus())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getCertificationStatus().compareTo(listing2.getCertificationStatus())) * sortFactor;
        }
    }
}
