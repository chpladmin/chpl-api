package gov.healthit.chpl.activity.search;

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

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("activitySearchRequestValidator")
public class SearchRequestValidator {
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public SearchRequestValidator(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
    }

    public void validate(SearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getTypeErrors(request));
        errors.addAll(getActivityDateErrors(request.getActivityDateStart(), request.getActivityDateEnd()));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getTypeErrors(SearchRequest request) {
        Set<String> typeErrors = new LinkedHashSet<String>();
        typeErrors.addAll(getTypeExistenceErrors(request.getTypes()));
        return typeErrors;
    }

    private Set<String> getTypeExistenceErrors(Set<String> types) {
        if (types == null || types.size() == 0) {
            return Collections.emptySet();
        }

        List<String> allowedTypes = Stream.of(ActivityConcept.values())
                .map(typeEnum -> typeEnum.toString())
                .toList();

        return types.stream()
            .filter(type -> !allowedTypes.contains(type))
            .map(type -> msgUtil.getMessage("search.activity.invalidType", type))
            .collect(Collectors.toSet());
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
                errors.add(msgUtil.getMessage("search.activity.activityDate.invalid",
                        activityDateStart, SearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(activityDateEnd)) {
            try {
                 LocalDate.parse(activityDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.activity.activityDate.invalid",
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
}
