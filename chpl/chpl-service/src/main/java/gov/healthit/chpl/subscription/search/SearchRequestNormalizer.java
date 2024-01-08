package gov.healthit.chpl.subscription.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeSearchTerm(request);
        normalizeSubscriptionSubjects(request);
        normalizeSubscriptionObjectTypes(request);
        normalizeSubscriberRoles(request);
        normalizeSubscriberStatuses(request);
        normalizeCreationDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        }
    }

    private void normalizeSubscriptionSubjects(SearchRequest request) {
        if (request.getSubscriptionSubjects() != null && request.getSubscriptionSubjects().size() > 0) {
            request.setSubscriptionSubjects(request.getSubscriptionSubjects().stream()
                    .filter(subj -> !StringUtils.isBlank(subj))
                    .map(subj -> StringUtils.normalizeSpace(subj))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeSubscriptionObjectTypes(SearchRequest request) {
        if (request.getSubscriptionObjectTypes() != null && request.getSubscriptionObjectTypes().size() > 0) {
            request.setSubscriptionObjectTypes(request.getSubscriptionObjectTypes().stream()
                    .filter(type -> !StringUtils.isBlank(type))
                    .map(type -> StringUtils.normalizeSpace(type))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeSubscriberRoles(SearchRequest request) {
        if (request.getSubscriberRoles() != null && request.getSubscriberRoles().size() > 0) {
            request.setSubscriberRoles(request.getSubscriberRoles().stream()
                    .filter(role -> !StringUtils.isBlank(role))
                    .map(role -> StringUtils.normalizeSpace(role))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeSubscriberStatuses(SearchRequest request) {
        if (request.getSubscriberStatuses() != null && request.getSubscriberStatuses().size() > 0) {
            request.setSubscriberStatuses(request.getSubscriberStatuses().stream()
                    .filter(status -> !StringUtils.isBlank(status))
                    .map(status -> StringUtils.normalizeSpace(status))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCreationDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getCreationDateTimeStart())) {
            request.setCreationDateTimeStart(request.getCreationDateTimeStart().trim());
        }
        if (!StringUtils.isEmpty(request.getCreationDateTimeEnd())) {
            request.setCreationDateTimeEnd(request.getCreationDateTimeEnd().trim());
        }
    }

    private void normalizeOrderBy(SearchRequest request) {
        if (!StringUtils.isBlank(request.getOrderByString())
                && request.getOrderBy() == null) {
            try {
                request.setOrderBy(
                        OrderByOption.valueOf(StringUtils.normalizeSpace(request.getOrderByString().toUpperCase())));
            } catch (Exception ignore) {
            }
        }
    }
}
