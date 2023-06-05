package gov.healthit.chpl.questionableactivity.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("questionableActivitySearchRequestValidator")
public class SearchRequestValidator {
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;

    private List<QuestionableActivityTrigger> allTriggerTypes;

    @Autowired
    public SearchRequestValidator(QuestionableActivityDAO questionableActivityDAO,
            ErrorMessageUtil msgUtil) {
        this.allTriggerTypes = questionableActivityDAO.getAllTriggers();
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
    }

    public void validate(SearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getTriggerIdErrors(request));
        errors.addAll(getActivityDateErrors(request.getActivityDateStart(), request.getActivityDateEnd()));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getTriggerIdErrors(SearchRequest request) {
        Set<String> triggerIdErrors = new LinkedHashSet<String>();
        triggerIdErrors.addAll(getTriggerIdExistenceErrors(request.getTriggerIds()));
        triggerIdErrors.addAll(getTriggerIdFormatErrors(request.getTriggerIdStrings()));
        return triggerIdErrors;
    }

    private Set<String> getTriggerIdExistenceErrors(Set<Long> triggerIds) {
        if (triggerIds == null || triggerIds.size() == 0) {
            return Collections.emptySet();
        }

        return triggerIds.stream()
            .filter(triggerId -> !isInSetOfTriggers(triggerId, allTriggerTypes))
            .map(triggerId -> msgUtil.getMessage("search.questionableActivity.invalidTrigger", triggerId.toString()))
            .collect(Collectors.toSet());
    }

    private Set<String> getTriggerIdFormatErrors(Set<String> triggerIdStrings) {
        if (triggerIdStrings != null && triggerIdStrings.size() > 0) {
            return triggerIdStrings.stream()
                .filter(triggerId -> !StringUtils.isBlank(triggerId))
                .filter(triggerId -> !isParseableLong(triggerId.trim()))
                .map(triggerId -> msgUtil.getMessage("search.questionableActivity.invalidTrigger", triggerId))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isParseableLong(String value) {
        try {
            Long.parseLong(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private Set<String> getActivityDateErrors(String activityDateStart, String activityDateEnd) {
        if (StringUtils.isEmpty(activityDateStart) && StringUtils.isEmpty(activityDateEnd)) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isEmpty(activityDateStart)) {
            try {
                 LocalDate.parse(activityDateStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.questionableActivity.activityDate.invalid",
                        activityDateStart, SearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(activityDateEnd)) {
            try {
                 LocalDate.parse(activityDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.questionableActivity.activityDate.invalid",
                        activityDateEnd, SearchRequest.DATE_SEARCH_FORMAT));
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

    private boolean isInSetOfTriggers(Long triggerId, List<QuestionableActivityTrigger> triggers) {
        if (triggers == null) {
            return false;
        }
        return triggers.stream()
            .filter(item -> item.getId().equals(triggerId))
            .count() > 0;
    }
}
