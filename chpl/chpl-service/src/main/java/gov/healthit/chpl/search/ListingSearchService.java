package gov.healthit.chpl.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CQMSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.IdNamePairSearchResult;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchResponse;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.util.DateUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("listingSearchService")
@NoArgsConstructor
@Log4j2
public class ListingSearchService {
    private static final String CURES_UPDATE_EDITION = "2015" + CertificationEdition.CURES_SUFFIX;
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private ListingSearchManager listingSearchManager;
    private DirectReviewSearchService drService;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ListingSearchService(SearchRequestValidator searchRequestValidator,
            ListingSearchManager listingSearchManager,
            DirectReviewSearchService drService) {
        this.searchRequestValidator = searchRequestValidator;
        this.listingSearchManager = listingSearchManager;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        this.drService = drService;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
    }

    public ListingSearchResponse findListings(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<ListingSearchResult> listings = listingSearchManager.getAllListings();
        LOGGER.debug("Total listings: " + listings.size());
        List<ListingSearchResult> matchedListings = listings.stream()
            .filter(listing -> matchesSearchTerm(listing, searchRequest.getSearchTerm()))
            .filter(listing -> matchesAcbNames(listing, searchRequest.getCertificationBodies()))
            .filter(listing -> matchesCertificationStatuses(listing, searchRequest.getCertificationStatuses()))
            .filter(listing -> matchesDerivedCertificationEditions(listing, searchRequest.getDerivedCertificationEditions()))
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
        LOGGER.debug("Total matched listings: " + matchedListings.size());

        ListingSearchResponse response = new ListingSearchResponse();
        response.setRecordCount(matchedListings.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());
        response.setDirectReviewsAvailable(drService.getDirectReviewsAvailable());

        sort(matchedListings, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<ListingSearchResult> pageOfListings
            = getPage(matchedListings, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfListings);
        return response;
    }

    private boolean matchesSearchTerm(ListingSearchResult listing, String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return true;
        }

        String searchTermUpperCase = searchTerm.toUpperCase();
        return (listing.getDeveloper() != null && !StringUtils.isEmpty(listing.getDeveloper().getName()) && listing.getDeveloper().getName().toUpperCase().contains(searchTermUpperCase))
                || (listing.getProduct() != null && !StringUtils.isEmpty(listing.getProduct().getName()) && listing.getProduct().getName().toUpperCase().contains(searchTermUpperCase))
                || (!CollectionUtils.isEmpty(listing.getPreviousDevelopers()) && doProductOwnersMatchSearchTerm(listing.getPreviousDevelopers(), searchTermUpperCase))
                || (!CollectionUtils.isEmpty(listing.getPreviousChplProductNumbers()) && doPreviousChplProductNumbersMatchSearchTerm(listing.getPreviousChplProductNumbers(), searchTermUpperCase))
                || (!StringUtils.isEmpty(listing.getChplProductNumber()) && listing.getChplProductNumber().toUpperCase().contains(searchTermUpperCase))
                || (!StringUtils.isEmpty(listing.getAcbCertificationId()) && listing.getAcbCertificationId().toUpperCase().contains(searchTermUpperCase));
    }

    private boolean doProductOwnersMatchSearchTerm(Set<IdNamePairSearchResult> productOwners, String searchTerm) {
        Set<String> uppercaseNames = productOwners.stream()
            .filter(productOwner -> !StringUtils.isEmpty(productOwner.getName()))
            .map(productOwner -> productOwner.getName().toUpperCase())
            .collect(Collectors.toSet());
        return uppercaseNames.stream()
                .filter(productOwnerName -> productOwnerName.contains(searchTerm))
                .findAny().isPresent();
    }

    private boolean doPreviousChplProductNumbersMatchSearchTerm(Set<String> previousChplProductNumbers, String searchTerm) {
        Set<String> uppercaseChplProductNumbers = previousChplProductNumbers.stream()
            .filter(chplProductNumber -> !StringUtils.isEmpty(chplProductNumber))
            .map(chplProductNumber -> chplProductNumber.toUpperCase())
            .collect(Collectors.toSet());
        return uppercaseChplProductNumbers.stream()
                .filter(chplProductNumber -> chplProductNumber.contains(searchTerm))
                .findAny().isPresent();
    }

    private boolean matchesAcbNames(ListingSearchResult listing, Set<String> acbNames) {
        if (CollectionUtils.isEmpty(acbNames)) {
            return true;
        }

        List<String> acbNamesUpperCase = acbNames.stream().map(acbName -> acbName.toUpperCase()).collect(Collectors.toList());
        return listing.getCertificationBody() != null
                && !StringUtils.isEmpty(listing.getCertificationBody().getName())
                && acbNamesUpperCase.contains(listing.getCertificationBody().getName().toUpperCase());
    }

    private boolean matchesCertificationStatuses(ListingSearchResult listing, Set<String> certificationStatuses) {
        if (CollectionUtils.isEmpty(certificationStatuses)) {
            return true;
        }

        List<String> certificationStatusesUpperCase = certificationStatuses.stream().map(certStatus -> certStatus.toUpperCase()).collect(Collectors.toList());
        return listing.getCertificationStatus() != null
                && !StringUtils.isEmpty(listing.getCertificationStatus().getName())
                && certificationStatusesUpperCase.contains(listing.getCertificationStatus().getName().toUpperCase());
    }

    private boolean matchesCertificationEditions(ListingSearchResult listing, Set<String> certificationEditions) {
        if (CollectionUtils.isEmpty(certificationEditions)) {
            return true;
        }

        return listing.getEdition() != null
                && !StringUtils.isEmpty(listing.getEdition().getName())
                && certificationEditions.contains(listing.getEdition().getName());
    }

    private boolean matchesDerivedCertificationEditions(ListingSearchResult listing, Set<String> derivedCertificationEditions) {
        if (CollectionUtils.isEmpty(derivedCertificationEditions)) {
            return true;
        }

        return derivedCertificationEditions.stream()
            .anyMatch(edition -> matchesDerivedCertificationEdition(listing, edition));
    }

    private boolean matchesDerivedCertificationEdition(ListingSearchResult listing, String derivedCertificationEdition) {
        boolean editionMatch = false;
        if (derivedCertificationEdition.equalsIgnoreCase(CURES_UPDATE_EDITION)) {
            editionMatch = listing.getEdition() != null
                    && !StringUtils.isEmpty(listing.getEdition().getName())
                    && listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                    && BooleanUtils.isTrue(listing.getCuresUpdate());
        } else if (derivedCertificationEdition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            editionMatch = listing.getEdition() != null
                    && !StringUtils.isEmpty(listing.getEdition().getName())
                    && listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                    && BooleanUtils.isFalse(listing.getCuresUpdate());
        } else {
            editionMatch = listing.getEdition() != null
                    && !StringUtils.isEmpty(listing.getEdition().getName())
                    && derivedCertificationEdition.equals(listing.getEdition().getName());
        }
        return editionMatch;
    }

    private boolean matchesDeveloper(ListingSearchResult listing, String developer) {
        if (StringUtils.isEmpty(developer)) {
            return true;
        }

        return listing.getDeveloper() != null
                && !StringUtils.isEmpty(listing.getDeveloper().getName())
                && listing.getDeveloper().getName().toUpperCase().contains(developer.toUpperCase());
    }

    private boolean matchesProduct(ListingSearchResult listing, String product) {
        if (StringUtils.isEmpty(product)) {
            return true;
        }

        return listing.getProduct() != null
                && !StringUtils.isEmpty(listing.getProduct().getName())
                && listing.getProduct().getName().toUpperCase().contains(product.toUpperCase());
    }

    private boolean matchesVersion(ListingSearchResult listing, String version) {
        if (StringUtils.isEmpty(version)) {
            return true;
        }

        return listing.getVersion() != null
                && !StringUtils.isEmpty(listing.getVersion().getName())
                && listing.getVersion().getName().toUpperCase().contains(version.toUpperCase());
    }

    private boolean matchesPracticeType(ListingSearchResult listing, String practiceType) {
        if (StringUtils.isEmpty(practiceType)) {
            return true;
        }

        return listing.getPracticeType() != null
                && !StringUtils.isEmpty(listing.getPracticeType().getName())
                && listing.getPracticeType().getName().toUpperCase().contains(practiceType.toUpperCase());
    }

    private boolean matchesCriteria(ListingSearchResult listing, Set<Long> criteriaIds, SearchSetOperator searchOperator) {
        if (CollectionUtils.isEmpty(criteriaIds)) {
            return true;
        }
        if (searchOperator.equals(SearchSetOperator.AND)) {
            return criteriaIds.stream()
                    .allMatch(criterionId -> getCriteriaIds(listing.getCriteriaMet()).contains(criterionId));
        } else if (searchOperator.equals(SearchSetOperator.OR)) {
            return criteriaIds.stream()
                    .anyMatch(criterionId -> getCriteriaIds(listing.getCriteriaMet()).contains(criterionId));
        }
        return false;
    }

    private Set<Long> getCriteriaIds(Set<CertificationCriterionSearchResult> criteria) {
        return criteria.stream()
                .map(criterion -> criterion.getId())
                .collect(Collectors.toSet());
    }

    private boolean matchesCqms(ListingSearchResult listing, Set<String> cqmNumbers, SearchSetOperator searchOperator) {
        if (CollectionUtils.isEmpty(cqmNumbers)) {
            return true;
        }
        if (searchOperator.equals(SearchSetOperator.AND)) {
            return cqmNumbers.stream()
                    .allMatch(cqmNumber -> getCqmNumbers(listing.getCqmsMet()).contains(cqmNumber));
        } else if (searchOperator.equals(SearchSetOperator.OR)) {
            return cqmNumbers.stream()
                    .anyMatch(cqmNumber -> getCqmNumbers(listing.getCqmsMet()).contains(cqmNumber));
        }
        return false;
    }

    private Set<String> getCqmNumbers(Set<CQMSearchResult> cqms) {
        return cqms.stream()
                .map(cqm -> cqm.getNumber())
                .collect(Collectors.toSet());
    }

    private boolean matchesComplianceFilter(ListingSearchResult listing, ComplianceSearchFilter complianceFilter) {
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

    private boolean matchesHasHadComplianceFilter(ListingSearchResult listing, Boolean hasHadComplianceFilter) {
        if (hasHadComplianceFilter == null) {
            return true;
        }
        if (hasHadComplianceFilter) {
            return listing.getSurveillanceCount() > 0 || listing.getDirectReviewCount() > 0;
        } else {
            return listing.getSurveillanceCount() == 0 && listing.getDirectReviewCount() == 0;
        }
    }

    private boolean matchesRwtFilter(ListingSearchResult listing, Set<RwtSearchOptions> rwtOptions, SearchSetOperator rwtOperator) {
        if (CollectionUtils.isEmpty(rwtOptions)) {
            return true;
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

        boolean matchesRwtFilter = applyOperation(rwtOperator,
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

    private boolean matchesCertificationDateRange(ListingSearchResult listing, String certificationDateRangeStart,
            String certificationDateRangeEnd) {
        if (StringUtils.isAllEmpty(certificationDateRangeStart, certificationDateRangeEnd)) {
            return true;
        }
        LocalDate startDate = parseLocalDate(certificationDateRangeStart);
        LocalDate endDate = parseLocalDate(certificationDateRangeEnd);
        if (listing.getCertificationDate() != null) {
            if (startDate == null && endDate != null) {
                return listing.getCertificationDate().isEqual(endDate) || listing.getCertificationDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return listing.getCertificationDate().isEqual(startDate) || listing.getCertificationDate().isAfter(startDate);
            } else {
                return listing.getCertificationDate().isEqual(startDate) || listing.getCertificationDate().isEqual(endDate)
                        || (listing.getCertificationDate().isBefore(endDate) && listing.getCertificationDate().isAfter(startDate));
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

    private List<ListingSearchResult> getPage(List<ListingSearchResult> listings, int beginIndex, int endIndex) {
        if (endIndex > listings.size()) {
            endIndex = listings.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<ListingSearchResult>();
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

    private void sort(List<ListingSearchResult> listings, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case DERIVED_EDITION:
                listings.sort(new DerivedEditionComparator(descending));
                break;
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

    private class DerivedEditionComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        DerivedEditionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getEdition(), listing2.getEdition())
                    || StringUtils.isAnyEmpty(listing1.getEdition().getName(), listing2.getEdition().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getDerivedEdition().compareTo(listing2.getDerivedEdition())) * sortFactor;
        }
    }

    private class EditionComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        EditionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getEdition(), listing2.getEdition())
                    || StringUtils.isAnyEmpty(listing1.getEdition().getName(), listing2.getEdition().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getEdition().getName().compareTo(listing2.getEdition().getName())) * sortFactor;
        }
    }

    private class DeveloperComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        DeveloperComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getDeveloper(), listing2.getDeveloper())
                    || StringUtils.isAnyEmpty(listing1.getDeveloper().getName(), listing2.getDeveloper().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getDeveloper().getName().compareTo(listing2.getDeveloper().getName())) * sortFactor;
        }
    }

    private class ProductComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        ProductComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getProduct(), listing2.getProduct())
                    || StringUtils.isAnyEmpty(listing1.getProduct().getName(), listing2.getProduct().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getProduct().getName().compareTo(listing2.getProduct().getName())) * sortFactor;
        }
    }

    private class VersionComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        VersionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getVersion(), listing2.getVersion())
                    || StringUtils.isAnyEmpty(listing1.getVersion().getName(), listing2.getVersion().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getVersion().getName().compareTo(listing2.getVersion().getName())) * sortFactor;
        }
    }

    private class CertificationDateComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        CertificationDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (listing1.getCertificationDate() == null ||  listing2.getCertificationDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getCertificationDate().compareTo(listing2.getCertificationDate())) * sortFactor;
        }
    }

    private class ChplIdComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        ChplIdComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getChplProductNumber(), listing2.getChplProductNumber())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getChplProductNumber().compareTo(listing2.getChplProductNumber())) * sortFactor;
        }
    }

    private class CertificationStatusComparator implements Comparator<ListingSearchResult> {
        private boolean descending = false;

        CertificationStatusComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ListingSearchResult listing1, ListingSearchResult listing2) {
            if (ObjectUtils.anyNull(listing1.getCertificationStatus(), listing2.getCertificationStatus())
                    || StringUtils.isAnyEmpty(listing1.getCertificationStatus().getName(), listing2.getCertificationStatus().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (listing1.getCertificationStatus().getName().compareTo(listing2.getCertificationStatus().getName())) * sortFactor;
        }
    }

    @Deprecated
    public SearchResponse search(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);
        ListingSearchResponse listingSearchResponse = findListings(searchRequest);

        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setDirectReviewsAvailable(listingSearchResponse.getDirectReviewsAvailable());
        searchResponse.setPageNumber(listingSearchResponse.getPageNumber());
        searchResponse.setPageSize(listingSearchResponse.getPageSize());
        searchResponse.setRecordCount(listingSearchResponse.getRecordCount());
        searchResponse.setResults(listingSearchResponse.getResults().stream()
                .map(listingSearchResult -> convertToBasicSearchResult(listingSearchResult))
                .collect(Collectors.toList()));
        return searchResponse;
    }

    private CertifiedProductBasicSearchResult convertToBasicSearchResult(ListingSearchResult searchResult) {
        return CertifiedProductBasicSearchResult.builder()
                .id(searchResult.getId())
                .chplProductNumber(searchResult.getChplProductNumber())
                .edition(searchResult.getEdition().getName())
                .curesUpdate(searchResult.getCuresUpdate())
                .acb(searchResult.getCertificationBody().getName())
                .acbCertificationId(searchResult.getAcbCertificationId())
                .practiceType(searchResult.getPracticeType() == null ? null : searchResult.getPracticeType().getName())
                .developerId(searchResult.getDeveloper().getId())
                .developer(searchResult.getDeveloper().getName())
                .developerStatus(searchResult.getDeveloper().getStatus().getName())
                .product(searchResult.getProduct().getName())
                .version(searchResult.getVersion().getName())
                .promotingInteroperabilityUserCount(searchResult.getPromotingInteroperability() == null ? null : searchResult.getPromotingInteroperability().getUserCount())
                .promotingInteroperabilityUserDate(searchResult.getPromotingInteroperability() == null ? null : searchResult.getPromotingInteroperability().getUserDate())
                .numMeaningfulUse(searchResult.getPromotingInteroperability() == null ? null : searchResult.getPromotingInteroperability().getUserCount())
                .numMeaningfulUseDate(searchResult.getPromotingInteroperability() == null ? null : DateUtil.toEpochMillis(searchResult.getPromotingInteroperability().getUserDate()))
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
                        searchResult.getServiceBaseUrlList() == null ? new HashSet<String>() :
                        Stream.of(
                                searchResult.getServiceBaseUrlList().getCriterion().getId() + CertifiedProductSearchResult.FROWNEY_SPLIT_CHAR
                                + searchResult.getServiceBaseUrlList().getValue())
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
                        .map(statusEvent -> DateUtil.format(statusEvent.getStatusStart()) + ":" + statusEvent.getStatus().getName())
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
}
