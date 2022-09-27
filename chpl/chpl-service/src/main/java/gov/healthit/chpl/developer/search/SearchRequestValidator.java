package gov.healthit.chpl.developer.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("developerSearchRequestValidator")
public class SearchRequestValidator {
    private DimensionalDataManager dimensionalDataManager;
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public SearchRequestValidator(DimensionalDataManager dimensionalDataManager,
            ErrorMessageUtil msgUtil) {
        this.dimensionalDataManager = dimensionalDataManager;
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
    }

    public void validate(SearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getStatusErrors(request.getStatuses()));
        errors.addAll(getAcbErrors(request.getCertificationBodies()));
        errors.addAll(getDecertificationDateErrors(request.getDecertificationDateStart(), request.getDecertificationDateEnd()));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getStatusErrors(Set<String> statuses) {
        if (statuses == null || statuses.size() == 0) {
            return Collections.emptySet();
        }

        Set<KeyValueModel> allowedDeveloperStatuses = dimensionalDataManager.getDeveloperStatuses();
        Set<String> allowedDeveloperStatusNames;
        if (!CollectionUtils.isEmpty(allowedDeveloperStatuses)) {
            allowedDeveloperStatusNames = allowedDeveloperStatuses.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allowedDeveloperStatusNames = Collections.emptySet();
        }

        return statuses.stream()
            .filter(status -> !isInSet(status, allowedDeveloperStatusNames))
            .map(status -> msgUtil.getMessage("search.developer.statuses.invalid", status,
                    allowedDeveloperStatusNames.stream().sorted().collect(Collectors.joining(", "))))
            .collect(Collectors.toSet());
    }

    private Set<String> getAcbErrors(Set<String> acbs) {
        if (acbs == null || acbs.size() == 0) {
            return Collections.emptySet();
        }

        Set<CertificationBody> allAcbs = dimensionalDataManager.getCertBodyNames();
        return acbs.stream()
                .filter(acb -> !isInAcbSet(acb, allAcbs))
                .map(acb -> msgUtil.getMessage("search.certificationBodies.invalid", acb))
                .collect(Collectors.toSet());
    }

    private Set<String> getDecertificationDateErrors(String decertificationDateStart, String decertificationDateEnd) {
        if (StringUtils.isEmpty(decertificationDateStart) && StringUtils.isEmpty(decertificationDateEnd)) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        LocalDate startDate = null, endDate = null;
        if (!StringUtils.isEmpty(decertificationDateStart)) {
            try {
                startDate = LocalDate.parse(decertificationDateStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.developer.decertificationDate.invalid", decertificationDateStart, SearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(decertificationDateEnd)) {
            try {
                endDate = LocalDate.parse(decertificationDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.developer.decertificationDate.invalid", decertificationDateEnd, SearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            errors.add(
                    msgUtil.getMessage("search.developer.decertificationDateOrder.invalid", decertificationDateEnd, decertificationDateStart));
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

    private boolean isInAcbSet(String value, Set<CertificationBody> setToSearch) {
        if (setToSearch == null) {
            return false;
        }
        return setToSearch.stream()
            .filter(item -> item.getName().equalsIgnoreCase(value))
            .count() > 0;
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
