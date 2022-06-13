package gov.healthit.chpl.changerequest.search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ChangeRequestSearchRequestValidator {
    private ChangeRequestManager changeRequestManager;
    private DeveloperDAO developerDao;
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ChangeRequestSearchRequestValidator(ChangeRequestManager changeRequestManager,
            DeveloperDAO developerDao,
            ErrorMessageUtil msgUtil) {
        this.changeRequestManager = changeRequestManager;
        this.developerDao = developerDao;
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public void validate(ChangeRequestSearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getDeveloperIdErrors(request));
        errors.addAll(getStatusNamesErrors(request));
        errors.addAll(getTypeNamesErrors(request));
        errors.addAll(getCurrentStatusChangeDateErrors(request));
        errors.addAll(getSubmittedDateErrors(request));
        errors.addAll(getPageSizeErrors(request));
        errors.addAll(getPageNumberErrors(request));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getDeveloperIdErrors(ChangeRequestSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!StringUtils.isEmpty(request.getDeveloperIdString())
                && request.getDeveloperId() == null) {
            errors.add(msgUtil.getMessage("search.changeRequest.developerId.invalidFormat", request.getDeveloperIdString()));
        }
        if (request.getDeveloperId() != null) {
            try {
                developerDao.getById(request.getDeveloperId());
            } catch (EntityRetrievalException ex) {
                errors.add(msgUtil.getMessage("search.changeRequest.developerId.doesNotExist", request.getDeveloperId().toString()));
            }
        }
        return errors;
    }

    private Set<String> getStatusNamesErrors(ChangeRequestSearchRequest request) {
        if (CollectionUtils.isEmpty(request.getCurrentStatusNames())) {
            return Collections.emptySet();
        }

        Set<KeyValueModel> allChangeRequestStatuses = changeRequestManager.getChangeRequestStatusTypes();
        Set<String> allChangeRequestStatusNames;
        if (!CollectionUtils.isEmpty(allChangeRequestStatuses)) {
            allChangeRequestStatusNames = allChangeRequestStatuses.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allChangeRequestStatusNames = Collections.emptySet();
        }
        return request.getCurrentStatusNames().stream()
            .filter(statusName -> !isInSet(statusName, allChangeRequestStatusNames))
            .map(statusName -> msgUtil.getMessage("search.changeRequest.statusName.invalid", statusName))
            .collect(Collectors.toSet());
    }

    private Set<String> getTypeNamesErrors(ChangeRequestSearchRequest request) {
        if (CollectionUtils.isEmpty(request.getTypeNames())) {
            return Collections.emptySet();
        }

        Set<KeyValueModel> allChangeRequestTypes = changeRequestManager.getChangeRequestTypes();
        Set<String> allChangeRequestTypeNames;
        if (!CollectionUtils.isEmpty(allChangeRequestTypes)) {
            allChangeRequestTypeNames = allChangeRequestTypes.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allChangeRequestTypeNames = Collections.emptySet();
        }
        return request.getTypeNames().stream()
            .filter(typeName -> !isInSet(typeName, allChangeRequestTypeNames))
            .map(typeName -> msgUtil.getMessage("search.changeRequest.typeName.invalid", typeName))
            .collect(Collectors.toSet());
    }

    private Set<String> getCurrentStatusChangeDateErrors(ChangeRequestSearchRequest request) {
        if (StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeStart())
                && StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeEnd())) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        LocalDateTime startDateTime = null, endDateTime = null;
        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeStart())) {
            try {
                startDateTime = LocalDateTime.parse(request.getCurrentStatusChangeDateTimeStart(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.changeRequest.currentStatusDateTime.invalid",
                        request.getCurrentStatusChangeDateTimeStart(),
                        ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeEnd())) {
            try {
                endDateTime = LocalDateTime.parse(request.getCurrentStatusChangeDateTimeEnd(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.changeRequest.currentStatusDateTime.invalid",
                        request.getCurrentStatusChangeDateTimeEnd(),
                        ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        if (endDateTime != null && startDateTime != null && endDateTime.isBefore(startDateTime)) {
            errors.add(msgUtil.getMessage("search.changeRequest.currentStatusDateTimes.invalidOrder",
                    endDateTime,
                    startDateTime));
        }
        return errors;
    }

    private Set<String> getSubmittedDateErrors(ChangeRequestSearchRequest request) {
        if (StringUtils.isEmpty(request.getSubmittedDateTimeStart())
                && StringUtils.isEmpty(request.getSubmittedDateTimeEnd())) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        LocalDateTime startDateTime = null, endDateTime = null;
        if (!StringUtils.isEmpty(request.getSubmittedDateTimeStart())) {
            try {
                startDateTime = LocalDateTime.parse(request.getSubmittedDateTimeStart(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.changeRequest.submittedDateTime.invalid",
                        request.getSubmittedDateTimeStart(),
                        ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(request.getSubmittedDateTimeEnd())) {
            try {
                endDateTime = LocalDateTime.parse(request.getSubmittedDateTimeEnd(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.changeRequest.submittedDateTime.invalid",
                        request.getSubmittedDateTimeEnd(),
                        ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT));
            }
        }

        if (endDateTime != null && startDateTime != null && endDateTime.isBefore(startDateTime)) {
            errors.add(msgUtil.getMessage("search.changeRequest.submittedDateTimes.invalidOrder",
                    endDateTime,
                    startDateTime));
        }
        return errors;
    }

    private Set<String> getPageNumberErrors(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageNumberString())
                && !isParseableInteger(request.getPageNumberString())) {
            return Stream.of(
                    msgUtil.getMessage("search.changeRequest.pageNumber.invalid", request.getPageNumberString()))
                    .collect(Collectors.toSet());
        }
        Integer pageNumber = request.getPageNumber();
        if (pageNumber != null && pageNumber < 0) {
            return Stream.of(
                    msgUtil.getMessage("search.changeRequest.pageNumber.outOfRange", request.getPageNumber()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getPageSizeErrors(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageSizeString())
                && !isParseableInteger(request.getPageSizeString())) {
            return Stream.of(
                    msgUtil.getMessage("search.changeRequest.pageSize.invalid", request.getPageSizeString()))
                    .collect(Collectors.toSet());
        }
        Integer pageSize = request.getPageSize();
        if (pageSize != null
                && (pageSize > ChangeRequestSearchRequest.MAX_PAGE_SIZE || pageSize < ChangeRequestSearchRequest.MIN_PAGE_SIZE)) {
            return Stream.of(
                    msgUtil.getMessage("search.changeRequest.pageSize.outOfRange",
                            ChangeRequestSearchRequest.MIN_PAGE_SIZE,
                            ChangeRequestSearchRequest.MAX_PAGE_SIZE,
                            pageSize.toString()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getOrderByErrors(ChangeRequestSearchRequest request) {
        if (request.getOrderBy() == null
                && !StringUtils.isBlank(request.getOrderByString())) {
            return Stream.of(msgUtil.getMessage("search.changeRequest.orderBy.invalid",
                    request.getOrderByString(),
                    Stream.of(OrderByOption.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isParseableInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private boolean isInSet(String value, Set<String> setToSearch) {
        if (setToSearch == null) {
            return false;
        }
        return setToSearch.stream()
            .filter(item -> item.equalsIgnoreCase(value))
            .count() > 0;
    }
}
