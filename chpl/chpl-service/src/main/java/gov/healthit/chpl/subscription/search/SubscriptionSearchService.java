package gov.healthit.chpl.subscription.search;

import java.time.LocalDateTime;
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
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SUBSCRIPTION, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).SEARCH)")
    public SubscriptionSearchResponse findSubscriptions(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<SubscriptionSearchResult> searchResults = subscriptionDao.getAllSubscriptions();
        LOGGER.debug("Total subscriptions: " + searchResults.size());
        List<SubscriptionSearchResult> matchedSubscriptions = searchResults.stream()
            .filter(sub -> matchesSearchTerm(sub, searchRequest.getSearchTerm()))
            .filter(sub -> matchesSubscriptionSubjects(sub, searchRequest.getSubscriptionSubjects()))
            .filter(sub -> matchesSubscriptionObjectTypes(sub, searchRequest.getSubscriptionObjectTypes()))
            .filter(sub -> matchesSubscriberRoles(sub, searchRequest.getSubscriberRoles()))
            .filter(sub -> matchesSubscriberStatuses(sub, searchRequest.getSubscriberStatuses()))
            .filter(dev -> matchesCreationDateRange(dev, searchRequest.getCreationDateTimeStart(), searchRequest.getCreationDateTimeEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched subscriptions: " + searchResults.size());

        SubscriptionSearchResponse response = new SubscriptionSearchResponse();
        response.setRecordCount(matchedSubscriptions.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

        sort(matchedSubscriptions, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<SubscriptionSearchResult> pageOfSubscriptions
            = getPage(matchedSubscriptions, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfSubscriptions);
        return response;
    }

    private boolean matchesSearchTerm(SubscriptionSearchResult subscription, String searchTerm) {
        return matchesSubscribedObjectName(subscription, searchTerm)
                || matchesSubscriberEmail(subscription, searchTerm);
    }

    private boolean matchesSubscribedObjectName(SubscriptionSearchResult subscription, String subscribedObjectName) {
        if (StringUtils.isEmpty(subscribedObjectName)) {
            return true;
        }

        return !StringUtils.isEmpty(subscription.getSubscribedObjectName())
                && subscription.getSubscribedObjectName().toUpperCase().contains(subscribedObjectName.toUpperCase());
    }

    private boolean matchesSubscriberEmail(SubscriptionSearchResult subscription, String subscriberEmail) {
        if (StringUtils.isEmpty(subscriberEmail)) {
            return true;
        }

        return !StringUtils.isEmpty(subscription.getSubscriberEmail())
                && subscription.getSubscriberEmail().toUpperCase().contains(subscriberEmail.toUpperCase());
    }

    private boolean matchesSubscriptionSubjects(SubscriptionSearchResult subscription, Set<String> subjectNames) {
        if (CollectionUtils.isEmpty(subjectNames)) {
            return true;
        }

        List<String> subjectNamesUpperCase = subjectNames.stream().map(name -> name.toUpperCase()).collect(Collectors.toList());
        return !CollectionUtils.isEmpty(subscription.getSubscriptionSubjects())
                && subscription.getSubscriptionSubjects().stream()
                    .filter(subSubj -> subjectNamesUpperCase.contains(subSubj.toUpperCase()))
                    .findAny().isPresent();
    }

    private boolean matchesSubscriptionObjectTypes(SubscriptionSearchResult subscription, Set<String> subscriptionObjectTypes) {
        if (CollectionUtils.isEmpty(subscriptionObjectTypes)) {
            return true;
        }

        List<String> subscriptionObjectTypesUpperCase = subscriptionObjectTypes.stream().map(name -> name.toUpperCase()).collect(Collectors.toList());
        return subscription.getSubscriptionObjectType() != null
                && !StringUtils.isEmpty(subscription.getSubscriptionObjectType())
                && subscriptionObjectTypesUpperCase.contains(subscription.getSubscriptionObjectType().toUpperCase());
    }

    private boolean matchesSubscriberRoles(SubscriptionSearchResult subscription, Set<String> subscriberRoles) {
        if (CollectionUtils.isEmpty(subscriberRoles)) {
            return true;
        }

        List<String> subscriberRolesUpperCase = subscriberRoles.stream().map(name -> name.toUpperCase()).collect(Collectors.toList());
        return subscription.getSubscriberRole() != null
                && !StringUtils.isEmpty(subscription.getSubscriberRole())
                && subscriberRolesUpperCase.contains(subscription.getSubscriberRole().toUpperCase());
    }

    private boolean matchesSubscriberStatuses(SubscriptionSearchResult subscription, Set<String> subscriberStatuses) {
        if (CollectionUtils.isEmpty(subscriberStatuses)) {
            return true;
        }

        List<String> subscriberStatusesUpperCase = subscriberStatuses.stream().map(name -> name.toUpperCase()).collect(Collectors.toList());
        return subscription.getSubscriberStatus() != null
                && !StringUtils.isEmpty(subscription.getSubscriberStatus())
                && subscriberStatusesUpperCase.contains(subscription.getSubscriberStatus().toUpperCase());
    }

    private boolean matchesCreationDateRange(SubscriptionSearchResult subscription, String creationDateRangeStart,
            String creationDateRangeEnd) {
        if (StringUtils.isAllEmpty(creationDateRangeStart, creationDateRangeEnd)) {
            return true;
        }
        LocalDateTime startDateTime = parseLocalDateTime(creationDateRangeStart);
        LocalDateTime endDateTime = parseLocalDateTime(creationDateRangeEnd);
        if (subscription.getCreationDate() != null) {
            if (startDateTime == null && endDateTime != null) {
                return subscription.getCreationDate().isEqual(endDateTime) || subscription.getCreationDate().isBefore(endDateTime);
            } else if (startDateTime != null && endDateTime == null) {
                return subscription.getCreationDate().isEqual(startDateTime) || subscription.getCreationDate().isAfter(startDateTime);
            } else {
                return (subscription.getCreationDate().isEqual(endDateTime) || subscription.getCreationDate().isBefore(endDateTime))
                        && (subscription.getCreationDate().isEqual(startDateTime) || subscription.getCreationDate().isAfter(startDateTime));
            }
        }
        return false;
    }

    private LocalDateTime parseLocalDateTime(String dateTimeString) {
        if (StringUtils.isEmpty(dateTimeString)) {
            return null;
        }

        LocalDateTime date = null;
        try {
            date = LocalDateTime.parse(dateTimeString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + dateTimeString + " as LocalDateTime of the format " + SearchRequest.TIMESTAMP_SEARCH_FORMAT);
        }
        return date;
    }

    private List<SubscriptionSearchResult> getPage(List<SubscriptionSearchResult> subscriptions, int beginIndex, int endIndex) {
        if (endIndex > subscriptions.size()) {
            endIndex = subscriptions.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<SubscriptionSearchResult>();
        }
        LOGGER.debug("Getting filtered subscriptions results between [" + beginIndex + ", " + endIndex + ")");
        return subscriptions.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(SearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(SearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<SubscriptionSearchResult> subscriptions, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case CREATION_DATE:
                subscriptions.sort(new CreationDateComparator(descending));
                break;
            case SUBSCRIBER_EMAIL:
                subscriptions.sort(new SubscriberEmailComparator(descending));
                break;
            case SUBSCRIBER_ROLE:
                subscriptions.sort(new SubscriberRoleComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class SubscriberEmailComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        SubscriberEmailComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult sub1, SubscriptionSearchResult sub2) {
            if (ObjectUtils.anyNull(sub1, sub2)
                    || StringUtils.isAnyEmpty(sub1.getSubscriberEmail(), sub2.getSubscriberEmail())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (sub1.getSubscriberEmail().compareTo(sub2.getSubscriberEmail())) * sortFactor;
        }
    }

    private class CreationDateComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        CreationDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult sub1, SubscriptionSearchResult sub2) {
            if (sub1.getCreationDate() == null ||  sub2.getCreationDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (sub1.getCreationDate().compareTo(sub2.getCreationDate())) * sortFactor;
        }
    }

    private class SubscriberRoleComparator implements Comparator<SubscriptionSearchResult> {
        private boolean descending = false;

        SubscriberRoleComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(SubscriptionSearchResult sub1, SubscriptionSearchResult sub2) {
            if (ObjectUtils.anyNull(sub1, sub2)
                    || StringUtils.isAnyEmpty(sub1.getSubscriberRole(), sub2.getSubscriberRole())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (sub1.getSubscriberRole().compareTo(sub2.getSubscriberRole())) * sortFactor;
        }
    }
}
