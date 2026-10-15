package gov.healthit.chpl.targeteduser.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("targetedUserSearchRequestValidator")
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
        errors.addAll(getIsUsedErrors(request.getIsUsed()));
        errors.addAll(getCreationDateErrors(request.getCreationDateStart(), request.getCreationDateEnd()));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getIsUsedErrors(String isUsed) {
        if (isUsed == null) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        boolean isValidBool = BooleanUtils.toBooleanObject(isUsed) != null;
        if (!isValidBool) {
            errors.add(msgUtil.getMessage("search.targetedUser.isUsed.invalid", isUsed));
        }
        return errors;
    }

    private Set<String> getCreationDateErrors(String creationDateStart, String creationDateEnd) {
        if (StringUtils.isEmpty(creationDateStart) && StringUtils.isEmpty(creationDateEnd)) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isEmpty(creationDateStart)) {
            try {
                 LocalDate.parse(creationDateStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.targetedUser.creationDate.invalid",
                        creationDateStart, SearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(creationDateEnd)) {
            try {
                 LocalDate.parse(creationDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.targetedUser.creationDate.invalid",
                        creationDateEnd, SearchRequest.DATE_SEARCH_FORMAT));
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
