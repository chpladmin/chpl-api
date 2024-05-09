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
import gov.healthit.chpl.search.domain.SearchSetOperator;
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
        dateFormatter = DateTimeFormatter.ofPattern(DeveloperSearchRequest.DATE_SEARCH_FORMAT);
    }

    public void validate(DeveloperSearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getStatusErrors(request.getStatuses()));
        errors.addAll(getAcbErrors(request.getAcbsForActiveListings()));
        errors.addAll(getAcbErrors(request.getAcbsForAllListings()));
        errors.addAll(getDecertificationDateErrors(request.getDecertificationDateStart(), request.getDecertificationDateEnd()));
        errors.addAll(getActiveListingsFilterErrors(request));
        errors.addAll(getAttestationsFilterErrors(request));
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

        Set<CertificationBody> allAcbs = dimensionalDataManager.getAllAcbs();
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
        if (!StringUtils.isEmpty(decertificationDateStart)) {
            try {
                LocalDate.parse(decertificationDateStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.developer.decertificationDate.invalid", decertificationDateStart, DeveloperSearchRequestV2.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(decertificationDateEnd)) {
            try {
                LocalDate.parse(decertificationDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.developer.decertificationDate.invalid", decertificationDateEnd, DeveloperSearchRequestV2.DATE_SEARCH_FORMAT));
            }
        }

        return errors;
    }

    private Set<String> getActiveListingsFilterErrors(DeveloperSearchRequest searchRequest) {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getActiveListingsOptionsOperatorErrors(searchRequest));
        errors.addAll(getActiveListingsOptionsErrors(searchRequest));
        return errors;
    }

    private Set<String> getActiveListingsOptionsOperatorErrors(DeveloperSearchRequest searchRequest) {
        if (searchRequest.getActiveListingsOptionsOperator() == null
                && !StringUtils.isBlank(searchRequest.getActiveListingsOptionsOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    searchRequest.getActiveListingsOptionsOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (isMissingActiveListingsOptionsOperator(searchRequest)
                && hasMultipleActiveListingsOptions(searchRequest)) {
            return Stream.of(msgUtil.getMessage("search.developer.missingActiveListingsOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isMissingActiveListingsOptionsOperator(DeveloperSearchRequest searchRequest) {
        return searchRequest.getActiveListingsOptionsOperator() == null;
    }

    private boolean hasMultipleActiveListingsOptions(DeveloperSearchRequest searchRequest) {
        return searchRequest.getActiveListingsOptions() != null && searchRequest.getActiveListingsOptions().size() > 1;
    }

    private Set<String> getActiveListingsOptionsErrors(DeveloperSearchRequest searchRequest) {
        if (searchRequest.getActiveListingsOptionsStrings() != null && searchRequest.getActiveListingsOptionsStrings().size() > 0) {
            return searchRequest.getActiveListingsOptionsStrings().stream()
                .filter(option -> !StringUtils.isBlank(option))
                .filter(option -> !isActiveListingsSearchOption(option))
                .map(option -> msgUtil.getMessage("search.developer.activeListingsSearchOption.invalid",
                        option,
                        Stream.of(ActiveListingSearchOptions.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isActiveListingsSearchOption(String option) {
        boolean result = true;
        try {
            ActiveListingSearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    private Set<String> getAttestationsFilterErrors(DeveloperSearchRequest searchRequest) {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getAttestationsOptionsOperatorErrors(searchRequest));
        errors.addAll(getAttestationsOptionsErrors(searchRequest));
        return errors;
    }

    private Set<String> getAttestationsOptionsOperatorErrors(DeveloperSearchRequest searchRequest) {
        if (searchRequest.getAttestationsOptionsOperator() == null
                && !StringUtils.isBlank(searchRequest.getAttestationsOptionsOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    searchRequest.getAttestationsOptionsOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (isMissingAttestationsOptionsOperator(searchRequest)
                && hasMultipleAttestationsOptions(searchRequest)) {
            return Stream.of(msgUtil.getMessage("search.developer.missingAttestationsOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isMissingAttestationsOptionsOperator(DeveloperSearchRequest searchRequest) {
        return searchRequest.getAttestationsOptionsOperator() == null;
    }

    private boolean hasMultipleAttestationsOptions(DeveloperSearchRequest searchRequest) {
        return searchRequest.getAttestationsOptions() != null && searchRequest.getAttestationsOptions().size() > 1;
    }

    private Set<String> getAttestationsOptionsErrors(DeveloperSearchRequest searchRequest) {
        if (searchRequest.getAttestationsOptionsStrings() != null && searchRequest.getAttestationsOptionsStrings().size() > 0) {
            return searchRequest.getAttestationsOptionsStrings().stream()
                .filter(option -> !StringUtils.isBlank(option))
                .filter(option -> !isAttestationsSearchOption(option))
                .map(option -> msgUtil.getMessage("search.developer.attestationsSearchOption.invalid",
                        option,
                        Stream.of(AttestationsSearchOptions.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isAttestationsSearchOption(String option) {
        boolean result = true;
        try {
            AttestationsSearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    private Set<String> getPageSizeErrors(Integer pageSize) {
        if (pageSize != null && pageSize > DeveloperSearchRequestV2.MAX_PAGE_SIZE) {
            return Stream.of(msgUtil.getMessage("search.pageSize.invalid", DeveloperSearchRequestV2.MAX_PAGE_SIZE))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getOrderByErrors(DeveloperSearchRequest searchRequest) {
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
