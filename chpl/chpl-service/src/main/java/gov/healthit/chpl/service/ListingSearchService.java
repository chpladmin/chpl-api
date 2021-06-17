package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.OrderByOption;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("listingSearchService")
@NoArgsConstructor
@Log4j2
public class ListingSearchService {
    private static final int MAX_PAGE_SIZE = 100;

    private ErrorMessageUtil msgUtil;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager cpSearchManager;

    @Autowired
    public ListingSearchService(ErrorMessageUtil msgUtil,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager cpSearchManager) {
        this.msgUtil = msgUtil;
        this.dimensionalDataManager = dimensionalDataManager;
        this.cpSearchManager = cpSearchManager;
    }

    public SearchResponse search(SearchRequest searchRequest) throws InvalidArgumentsException {
        //TODO: validate parameters
        //TODO: trim parameters

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

            //TODO: certification dates
            //TODO: criteria
            //TODO: cqms
            //TODO: compliance
            .collect(Collectors.toList());
        LOGGER.debug("Total filtered listings: " + filteredListings.size());

        SearchResponse response = new SearchResponse();
        response.setRecordCount(filteredListings.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

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
        if (certificationEditions == null || certificationEditions.size() == 0) {
            return true;
        }

        return !StringUtils.isEmpty(listing.getEdition()) && certificationEditions.contains(listing.getEdition());
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
                listings.sort(new EditionComparator());
                break;
            case DEVELOPER:
                listings.sort(new DeveloperComparator());
                break;
            case PRODUCT:
                listings.sort(new ProductComparator());
                break;
            case VERSION:
                listings.sort(new VersionComparator());
                break;
            case CERTIFICATION_DATE:
                listings.sort(new CertificationDateComparator());
                break;
            case CHPL_ID:
                listings.sort(new ChplIdComparator());
                break;
            case STATUS:
                listings.sort(new CertificationStatusComparator());
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class EditionComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getEdition(), listing2.getEdition())) {
                return 0;
            }
            return listing1.getEdition().compareTo(listing2.getEdition());
        }
    }

    private class DeveloperComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getDeveloper(), listing2.getDeveloper())) {
                return 0;
            }
            return listing1.getDeveloper().compareTo(listing2.getDeveloper());
        }
    }

    private class ProductComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getProduct(), listing2.getProduct())) {
                return 0;
            }
            return listing1.getProduct().compareTo(listing2.getProduct());
        }
    }

    private class VersionComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getVersion(), listing2.getVersion())) {
                return 0;
            }
            return listing1.getVersion().compareTo(listing2.getVersion());
        }
    }

    private class CertificationDateComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (listing1.getCertificationDate() == null ||  listing2.getCertificationDate() == null) {
                return 0;
            }
            return listing1.getCertificationDate().compareTo(listing2.getCertificationDate());
        }
    }

    private class ChplIdComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getChplProductNumber(), listing2.getChplProductNumber())) {
                return 0;
            }
            return listing1.getChplProductNumber().compareTo(listing2.getChplProductNumber());
        }
    }

    private class CertificationStatusComparator implements Comparator<CertifiedProductBasicSearchResult> {
        @Override
        public int compare(CertifiedProductBasicSearchResult listing1, CertifiedProductBasicSearchResult listing2) {
            if (StringUtils.isAnyEmpty(listing1.getCertificationStatus(), listing2.getCertificationStatus())) {
                return 0;
            }
            return listing1.getCertificationStatus().compareTo(listing2.getCertificationStatus());
        }
    }
}
