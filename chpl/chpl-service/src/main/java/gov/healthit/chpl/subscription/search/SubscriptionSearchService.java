package gov.healthit.chpl.subscription.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("subscriptionSearchService")
@NoArgsConstructor
@Log4j2
public class SubscriptionSearchService {
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private SubscriptionDao subscriptionDao;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public SubscriptionSearchService(@Qualifier("subscriptionSearchRequestValidator") SearchRequestValidator searchRequestValidator,
            SubscriptionDao subscriptionDao) {
        this.searchRequestValidator = searchRequestValidator;
        this.subscriptionDao = subscriptionDao;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.TIMESTAMP_SEARCH_FORMAT);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SUBSCRIPTION, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).SEARCH)")
    public SubscriptionSearchResponse findSubscriptions(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<SubscriptionSearchResult> searchResults = subscriptionDao.getAllSubscriptions();
        LOGGER.debug("Total developers: " + developers.size());
        List<SubscriptionSearchResult> matchedDevelopers = developers.stream()
            .filter(dev -> matchesSearchTerm(dev, searchRequest.getSearchTerm()))
            .filter(dev -> matchesDeveloperName(dev, searchRequest.getDeveloperName()))
            .filter(dev -> matchesDeveloperCode(dev, searchRequest.getDeveloperCode()))
            .filter(dev -> matchesAcbNames(dev, searchRequest.getCertificationBodies()))
            .filter(dev -> matchesStatuses(dev, searchRequest.getStatuses()))
            .filter(dev -> matchesDecertificationDateRange(dev, searchRequest.getDecertificationDateStart(), searchRequest.getDecertificationDateEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched developers: " + matchedDevelopers.size());

        SubscriptionSearchResponse response = new SubscriptionSearchResponse();
        response.setRecordCount(matchedDevelopers.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

        sort(matchedDevelopers, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<SubscriptionSearchResult> pageOfDevelopers
            = getPage(matchedDevelopers, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfDevelopers);
        return response;
    }

    private boolean matchesSearchTerm(SubscriptionSearchResult developer, String searchTerm) {
        return matchesDeveloperName(developer, searchTerm)
                || matchesDeveloperCode(developer, searchTerm);
    }

    private boolean matchesDeveloperName(SubscriptionSearchResult developer, String developerName) {
        if (StringUtils.isEmpty(developerName)) {
            return true;
        }

        return !StringUtils.isEmpty(developer.getName())
                && developer.getName().toUpperCase().contains(developerName.toUpperCase());
    }

    private boolean matchesDeveloperCode(SubscriptionSearchResult developer, String developerCode) {
        if (StringUtils.isEmpty(developerCode)) {
            return true;
        }

        return !StringUtils.isEmpty(developer.getCode())
                && developer.getCode().toUpperCase().contains(developerCode.toUpperCase());
    }

    private boolean matchesAcbNames(SubscriptionSearchResult developer, Set<String> acbNames) {
        if (CollectionUtils.isEmpty(acbNames)) {
            return true;
        }

        List<String> acbNamesUpperCase = acbNames.stream().map(acbName -> acbName.toUpperCase()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(developer.getAssociatedAcbs())) {
            return false;
        }
        Set<String> developerAcbNamesUpperCase = developer.getAssociatedAcbs().stream()
                .map(acb -> acb.getName().toUpperCase())
                .collect(Collectors.toSet());
        developerAcbNamesUpperCase.retainAll(acbNamesUpperCase);
        return !CollectionUtils.isEmpty(developerAcbNamesUpperCase);
    }

    private boolean matchesStatuses(SubscriptionSearchResult developer, Set<String> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return true;
        }

        List<String> statusesUpperCase = statuses.stream().map(status -> status.toUpperCase()).collect(Collectors.toList());
        return developer.getStatus() != null
                && !StringUtils.isEmpty(developer.getStatus().getName())
                && statusesUpperCase.contains(developer.getStatus().getName().toUpperCase());
    }

    private boolean matchesDecertificationDateRange(SubscriptionSearchResult developer, String decertificationDateRangeStart,
            String decertificationDateRangeEnd) {
        if (StringUtils.isAllEmpty(decertificationDateRangeStart, decertificationDateRangeEnd)) {
            return true;
        }
        LocalDate startDate = parseLocalDate(decertificationDateRangeStart);
        LocalDate endDate = parseLocalDate(decertificationDateRangeEnd);
        if (developer.getDecertificationDate() != null) {
            if (startDate == null && endDate != null) {
                return developer.getDecertificationDate().isEqual(endDate) || developer.getDecertificationDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return developer.getDecertificationDate().isEqual(startDate) || developer.getDecertificationDate().isAfter(startDate);
            } else {
                return (developer.getDecertificationDate().isEqual(endDate) || developer.getDecertificationDate().isBefore(endDate))
                        && (developer.getDecertificationDate().isEqual(startDate) || developer.getDecertificationDate().isAfter(startDate));
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
            LOGGER.error("Cannot parse " + dateString + " as date of the format " + SearchRequest.DATE_SEARCH_FORMAT);
        }
        return date;
    }

    private List<SubscriptionSearchResult> getPage(List<SubscriptionSearchResult> developers, int beginIndex, int endIndex) {
        if (endIndex > developers.size()) {
            endIndex = developers.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<SubscriptionSearchResult>();
        }
        LOGGER.debug("Getting filtered developer results between [" + beginIndex + ", " + endIndex + ")");
        return developers.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(SearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(SearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<SubscriptionSearchResult> developers, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case STATUS:
                developers.sort(new StatusComparator(descending));
                break;
            case DECERTIFICATION_DATE:
                developers.sort(new DecertificationDateComparator(descending));
                break;
            case DEVELOPER:
                developers.sort(new DeveloperComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class DeveloperComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        DeveloperComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult dev1, SubscriptionSearchResult dev2) {
            if (ObjectUtils.anyNull(dev1, dev2)
                    || StringUtils.isAnyEmpty(dev1.getName(), dev2.getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (dev1.getName().compareTo(dev2.getName())) * sortFactor;
        }
    }

    private class DecertificationDateComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        DecertificationDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult dev1, SubscriptionSearchResult dev2) {
            if (dev1.getDecertificationDate() == null ||  dev2.getDecertificationDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (dev1.getDecertificationDate().compareTo(dev2.getDecertificationDate())) * sortFactor;
        }
    }

    private class StatusComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        StatusComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult dev1, SubscriptionSearchResult dev2) {
            if (ObjectUtils.anyNull(dev1.getStatus(), dev2.getStatus())
                    || StringUtils.isAnyEmpty(dev1.getStatus().getName(), dev2.getStatus().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (dev1.getStatus().getName().compareTo(dev2.getStatus().getName())) * sortFactor;
        }
    }
}
