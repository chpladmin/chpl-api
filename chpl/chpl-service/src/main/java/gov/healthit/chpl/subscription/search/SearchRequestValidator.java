package gov.healthit.chpl.subscription.search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("subscriptionSearchRequestValidator")
public class SearchRequestValidator {
    private ErrorMessageUtil msgUtil;

    private List<SubscriptionSubject> allowedSubscriptionSubjects;
    private List<SubscriptionObjectType> allowedSubscriptionObjectTypes;
    private List<SubscriberRole> allowedSubscriberRoles;
    private List<SubscriberStatus> allowedSubscriberStatuses;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public SearchRequestValidator(SubscriptionDao subscriptionDao, SubscriberDao subscriberDao,
            ErrorMessageUtil msgUtil) {
        allowedSubscriptionSubjects = subscriptionDao.getAllSubjects();
        allowedSubscriptionObjectTypes = subscriptionDao.getAllSubscriptionObjectTypes();
        allowedSubscriberRoles = subscriberDao.getAllRoles();
        allowedSubscriberStatuses = subscriberDao.getAllStatuses();
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        this.msgUtil = msgUtil;
    }

    public void validate(SearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getSubscriptionSubjectErrors(request.getSubscriptionSubjects()));
        errors.addAll(getSubscriptionObjectTypeErrors(request.getSubscriptionObjectTypes()));
        errors.addAll(getSubscriberRoleErrors(request.getSubscriberRoles()));
        errors.addAll(getSubscriberStatusErrors(request.getSubscriberStatuses()));
        errors.addAll(getCreationDateErrors(request.getCreationDateTimeStart(), request.getCreationDateTimeEnd()));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getSubscriptionSubjectErrors(Set<String> subscriptionSubjects) {
        if (subscriptionSubjects == null || subscriptionSubjects.size() == 0) {
            return Collections.emptySet();
        }

        List<String> allowedSubscriptionSubjectNames;
        if (!CollectionUtils.isEmpty(allowedSubscriptionSubjects)) {
            allowedSubscriptionSubjectNames = allowedSubscriptionSubjects.stream().map(kvm -> kvm.getSubject()).collect(Collectors.toList());
        } else {
            allowedSubscriptionSubjectNames = Collections.emptyList();
        }

        return subscriptionSubjects.stream()
            .filter(subj -> !isInCollection(subj, allowedSubscriptionSubjectNames))
            .map(subj -> msgUtil.getMessage("search.subscriptions.subject.invalid", subj,
                    Util.joinListGrammatically(allowedSubscriptionSubjectNames, "or")))
            .collect(Collectors.toSet());
    }

    private Set<String> getSubscriptionObjectTypeErrors(Set<String> subscriptionObjectTypes) {
        if (subscriptionObjectTypes == null || subscriptionObjectTypes.size() == 0) {
            return Collections.emptySet();
        }

        List<String> allowedSubscriptionObjectTypeNames;
        if (!CollectionUtils.isEmpty(allowedSubscriptionObjectTypes)) {
            allowedSubscriptionObjectTypeNames = allowedSubscriptionObjectTypes.stream().map(kvm -> kvm.getName()).collect(Collectors.toList());
        } else {
            allowedSubscriptionObjectTypeNames = Collections.emptyList();
        }

        return subscriptionObjectTypes.stream()
            .filter(type -> !isInCollection(type, allowedSubscriptionObjectTypeNames))
            .map(type -> msgUtil.getMessage("search.subscriptions.objectType.invalid", type,
                    Util.joinListGrammatically(allowedSubscriptionObjectTypeNames, "or")))
            .collect(Collectors.toSet());
    }

    private Set<String> getSubscriberRoleErrors(Set<String> subscriberRoles) {
        if (subscriberRoles == null || subscriberRoles.size() == 0) {
            return Collections.emptySet();
        }

        List<String> allowedSubscriberRoleNames;
        if (!CollectionUtils.isEmpty(allowedSubscriberRoles)) {
            allowedSubscriberRoleNames = allowedSubscriberRoles.stream().map(kvm -> kvm.getName()).collect(Collectors.toList());
        } else {
            allowedSubscriberRoleNames = Collections.emptyList();
        }

        return subscriberRoles.stream()
            .filter(role -> !isInCollection(role, allowedSubscriberRoleNames))
            .map(role -> msgUtil.getMessage("search.subscriptions.role.invalid", role,
                    Util.joinListGrammatically(allowedSubscriberRoleNames), "or"))
            .collect(Collectors.toSet());
    }

    private Set<String> getSubscriberStatusErrors(Set<String> subscriberStatuses) {
        if (subscriberStatuses == null || subscriberStatuses.size() == 0) {
            return Collections.emptySet();
        }

        List<String> allowedSubscriberStatusNames;
        if (!CollectionUtils.isEmpty(allowedSubscriberStatuses)) {
            allowedSubscriberStatusNames = allowedSubscriberStatuses.stream().map(kvm -> kvm.getName()).collect(Collectors.toList());
        } else {
            allowedSubscriberStatusNames = Collections.emptyList();
        }

        return subscriberStatuses.stream()
            .filter(status -> !isInCollection(status, allowedSubscriberStatusNames))
            .map(status -> msgUtil.getMessage("search.subscriptions.status.invalid", status,
                    Util.joinListGrammatically(allowedSubscriberStatusNames), "or"))
            .collect(Collectors.toSet());
    }

    private Set<String> getCreationDateErrors(String creationDateTimeStart, String creationDateTimeEnd) {
        if (StringUtils.isEmpty(creationDateTimeStart) && StringUtils.isEmpty(creationDateTimeEnd)) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isEmpty(creationDateTimeStart)) {
            try {
                LocalDateTime.parse(creationDateTimeStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.subscriptions.creationDateTime.invalid",
                        creationDateTimeStart,
                        SearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(creationDateTimeEnd)) {
            try {
                LocalDateTime.parse(creationDateTimeEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.subscriptions.creationDateTime.invalid",
                        creationDateTimeEnd,
                        SearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        return errors;
    }

    private Set<String> getPageSizeErrors(Integer pageSize) {
        if (pageSize != null && pageSize > SearchRequest.MAX_PAGE_SIZE) {
            return Stream.of(msgUtil.getMessage("search.pageSize.invalid", SearchRequest.MAX_PAGE_SIZE))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getOrderByErrors(SearchRequest searchRequest) {
        if (searchRequest.getOrderBy() == null
                && !StringUtils.isBlank(searchRequest.getOrderByString())) {
            return Stream.of(msgUtil.getMessage("search.orderBy.invalid",
                    searchRequest.getOrderByString(),
                    Stream.of(OrderByOption.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isInCollection(String value, Collection<String> collToSearch) {
        if (collToSearch == null) {
            return false;
        }
        return collToSearch.stream()
            .filter(item -> item.equalsIgnoreCase(value))
            .count() > 0;
    }
}
