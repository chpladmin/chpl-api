package gov.healthit.chpl.search;

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

import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SearchRequestValidator {
    private DimensionalDataManager dimensionalDataManager;
    private DirectReviewSearchService drService;
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;
    private Set<String> allowedDerivedCertificationEditions;

    @Autowired
    public SearchRequestValidator(DimensionalDataManager dimensionalDataManager,
            DirectReviewSearchService drService,
            ErrorMessageUtil msgUtil) {
        this.dimensionalDataManager = dimensionalDataManager;
        this.drService = drService;
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
        allowedDerivedCertificationEditions = Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear(),
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear(),
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear(),
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear() + CertificationEdition.CURES_SUFFIX)
                .map(editionName -> editionName.toUpperCase())
                .collect(Collectors.toSet());
    }

    public void validate(SearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getListingIdErrors(request.getListingIdStrings()));
        errors.addAll(getCertificationStatusErrors(request.getCertificationStatuses()));
        errors.addAll(getDerivedCertificationEditionErrors(request.getDerivedCertificationEditions()));
        errors.addAll(getCertificationEditionErrors(request.getCertificationEditions()));
        errors.addAll(getCertificationCriteriaErrors(request));
        errors.addAll(getCertificationCriteriaOperatorErrors(request));
        errors.addAll(getCqmErrors(request.getCqms()));
        errors.addAll(getCqmOperatorErrors(request));
        errors.addAll(getAcbErrors(request.getCertificationBodies()));
        errors.addAll(getPracticeTypeErrors(request.getPracticeType()));
        errors.addAll(getCertificationDateErrors(request.getCertificationDateStart(), request.getCertificationDateEnd()));
        errors.addAll(getComplianceActivityErrors(request.getComplianceActivity()));
        errors.addAll(getRwtOptionsErrors(request));
        errors.addAll(getRwtOperatorErrors(request));
        errors.addAll(getPageSizeErrors(request.getPageSize()));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getListingIdErrors(Set<String> listingIdStrings) {
        Set<String> listingIdErrors = new LinkedHashSet<String>();
        listingIdErrors.addAll(getListingIdFormatErrors(listingIdStrings));
        return listingIdErrors;
    }

    private Set<String> getListingIdFormatErrors(Set<String> listingIdStrings) {
        if (!CollectionUtils.isEmpty(listingIdStrings)) {
            return listingIdStrings.stream()
                .filter(listingId -> !StringUtils.isBlank(listingId))
                .filter(listingId -> !isParseableLong(listingId.trim()))
                .map(listingId -> msgUtil.getMessage("search.listingId.invalid", listingId))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getCertificationStatusErrors(Set<String> certificationStatuses) {
        if (certificationStatuses == null || certificationStatuses.size() == 0) {
            return Collections.emptySet();
        }

        Set<KeyValueModel> allCertificationStatuses = dimensionalDataManager.getCertificationStatuses();
        Set<String> allCertificationStatusNames;
        if (!CollectionUtils.isEmpty(allCertificationStatuses)) {
            allCertificationStatusNames = allCertificationStatuses.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allCertificationStatusNames = Collections.emptySet();
        }
        return certificationStatuses.stream()
            .filter(certificationStatus -> !isInSet(certificationStatus, allCertificationStatusNames))
            .map(certificationStatus -> msgUtil.getMessage("search.certificationStatuses.invalid", certificationStatus))
            .collect(Collectors.toSet());
    }

    private Set<String> getDerivedCertificationEditionErrors(Set<String> derivedCertificationEditions) {
        if (CollectionUtils.isEmpty(derivedCertificationEditions)) {
            return Collections.emptySet();
        }

        return derivedCertificationEditions.stream()
            .filter(certificationEdition -> !isInSet(certificationEdition.toUpperCase(), allowedDerivedCertificationEditions))
            .map(certificationEdition -> msgUtil.getMessage("search.derivedCertificationEdition.invalid", certificationEdition))
            .collect(Collectors.toSet());
    }

    private Set<String> getCertificationEditionErrors(Set<String> certificationEditions) {
        if (certificationEditions == null || certificationEditions.size() == 0) {
            return Collections.emptySet();
        }

        Set<String> allYears = new LinkedHashSet<String>();
        Set<KeyValueModel> allCertificationEditions = dimensionalDataManager.getEditionNames(false);
        if (!CollectionUtils.isEmpty(allCertificationEditions)) {
            allYears.addAll(allCertificationEditions.stream()
                    .map(keyValueModel -> keyValueModel.getName().toUpperCase())
                    .collect(Collectors.toList()));
        }
        return certificationEditions.stream()
            .filter(certificationEdition -> !isInSet(certificationEdition.toUpperCase(), allYears))
            .map(certificationEdition -> msgUtil.getMessage("search.certificationEdition.invalid", certificationEdition))
            .collect(Collectors.toSet());
    }

    private Set<String> getCertificationCriteriaErrors(SearchRequest request) {
        Set<String> criteriaErrors = new LinkedHashSet<String>();
        criteriaErrors.addAll(getCriteriaExistenceErrors(request.getCertificationCriteriaIds()));
        criteriaErrors.addAll(getCriteriaIdFormatErrors(request.getCertificationCriteriaIdStrings()));
        return criteriaErrors;
    }

    private Set<String> getCriteriaExistenceErrors(Set<Long> certificationCriteriaIds) {
        if (certificationCriteriaIds == null || certificationCriteriaIds.size() == 0) {
            return Collections.emptySet();
        }

        Set<CertificationCriterion> allCriteria = dimensionalDataManager.getCertificationCriterion();
        return certificationCriteriaIds.stream()
            .filter(certificationCriteriaId -> !isInSet(certificationCriteriaId, allCriteria))
            .map(certificationCriteriaId -> msgUtil.getMessage("search.certificationCriteria.invalid", certificationCriteriaId.toString()))
            .collect(Collectors.toSet());
    }

    private Set<String> getCriteriaIdFormatErrors(Set<String> criteriaIdStrings) {
        if (criteriaIdStrings != null && criteriaIdStrings.size() > 0) {
            return criteriaIdStrings.stream()
                .filter(criteriaId -> !StringUtils.isBlank(criteriaId))
                .filter(criteriaId -> !isParseableLong(criteriaId.trim()))
                .map(criteriaId -> msgUtil.getMessage("search.certificationCriteriaId.invalid", criteriaId))
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

    private Set<String> getCertificationCriteriaOperatorErrors(SearchRequest searchRequest) {
        if (searchRequest.getCertificationCriteriaOperator() == null
                && !StringUtils.isBlank(searchRequest.getCertificationCriteriaOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    searchRequest.getCertificationCriteriaOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (searchRequest.getCertificationCriteriaOperator() == null
                && StringUtils.isBlank(searchRequest.getCertificationCriteriaOperatorString())
                && hasMultipleCriteriaToSearch(searchRequest)) {
            return Stream.of(msgUtil.getMessage("search.certificationCriteria.missingSearchOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean hasMultipleCriteriaToSearch(SearchRequest searchRequest) {
        return searchRequest.getCertificationCriteriaIds() != null && searchRequest.getCertificationCriteriaIds().size() > 1;
    }

    private Set<String> getCqmErrors(Set<String> cqmNumbers) {
        if (cqmNumbers == null || cqmNumbers.size() == 0) {
            return Collections.emptySet();
        }

        Set<DescriptiveModel> allCqms = dimensionalDataManager.getCQMCriterionNumbers(false);
        Set<String> allCqmNumbers;
        if (!CollectionUtils.isEmpty(allCqms)) {
            allCqmNumbers = allCqms.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allCqmNumbers = Collections.emptySet();
        }
        return cqmNumbers.stream()
                .filter(cqm -> !isInSet(cqm, allCqmNumbers))
                .map(cqm -> msgUtil.getMessage("search.cqms.invalid", cqm))
                .collect(Collectors.toSet());
    }

    private Set<String> getCqmOperatorErrors(SearchRequest searchRequest) {
        if (searchRequest.getCqmsOperator() == null
                && !StringUtils.isBlank(searchRequest.getCqmsOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    searchRequest.getCqmsOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (searchRequest.getCqmsOperator() == null
                && StringUtils.isBlank(searchRequest.getCqmsOperatorString())
                && hasMultipleCqmsToSearch(searchRequest)) {
            return Stream.of(msgUtil.getMessage("search.cqms.missingSearchOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean hasMultipleCqmsToSearch(SearchRequest searchRequest) {
        return searchRequest.getCqms() != null && searchRequest.getCqms().size() > 1;
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

    private Set<String> getPracticeTypeErrors(String practiceType) {
        if (StringUtils.isEmpty(practiceType)) {
            return Collections.emptySet();
        }

        Set<KeyValueModel> allPracticeTypes = dimensionalDataManager.getPracticeTypeNames();
        Set<String> allPracticeTypeNames;
        if (!CollectionUtils.isEmpty(allPracticeTypes)) {
            allPracticeTypeNames = allPracticeTypes.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allPracticeTypeNames = Collections.emptySet();
        }
        return Stream.of(practiceType)
                .filter(ptype -> !isInSet(ptype, allPracticeTypeNames))
                .map(ptype -> msgUtil.getMessage("search.practiceType.invalid", ptype))
                .collect(Collectors.toSet());
    }

    private Set<String> getCertificationDateErrors(String certificationDateStart, String certificationDateEnd) {
        if (StringUtils.isEmpty(certificationDateStart) && StringUtils.isEmpty(certificationDateEnd)) {
            return Collections.emptySet();
        }

        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isEmpty(certificationDateStart)) {
            try {
                 LocalDate.parse(certificationDateStart, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.certificationDate.invalid", certificationDateStart, SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isEmpty(certificationDateEnd)) {
            try {
                LocalDate.parse(certificationDateEnd, dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.certificationDate.invalid", certificationDateEnd, SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT));
            }
        }

        return errors;
    }

    private Set<String> getComplianceActivityErrors(ComplianceSearchFilter complianceFilter) {
        Set<String> errors = new LinkedHashSet<String>();
        if (hasAnyComplianceFilters(complianceFilter) && !drService.doesCacheHaveAnyOkData()) {
            errors.add(msgUtil.getMessage("search.complianceFilter.unavailable"));
        }
        errors.addAll(getNonConformityOperatorErrors(complianceFilter));
        errors.addAll(getNonConformitySearchOptionsErrors(complianceFilter));
        return errors;
    }

    private boolean hasAnyComplianceFilters(ComplianceSearchFilter complianceFilter) {
        return complianceFilter != null
                && (complianceFilter.getHasHadComplianceActivity() != null
                    || (complianceFilter.getNonConformityOptions() != null && complianceFilter.getNonConformityOptions().size() > 0)
                    || complianceFilter.getNonConformityOptionsOperator() != null);
    }

    private Set<String> getNonConformityOperatorErrors(ComplianceSearchFilter complianceFilter) {
        if (complianceFilter != null && complianceFilter.getNonConformityOptionsOperator() == null
                && !StringUtils.isBlank(complianceFilter.getNonConformityOptionsOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    complianceFilter.getNonConformityOptionsOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (complianceFilter != null && isMissingOptionsOperator(complianceFilter)
                && hasMultipleOptions(complianceFilter)) {
            return Stream.of(msgUtil.getMessage("search.compliance.missingSearchOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isMissingOptionsOperator(ComplianceSearchFilter complianceFilter) {
        return complianceFilter.getNonConformityOptionsOperator() == null;
    }

    private boolean hasMultipleOptions(ComplianceSearchFilter complianceFilter) {
        return complianceFilter.getNonConformityOptions() != null && complianceFilter.getNonConformityOptions().size() > 1;
    }

    private Set<String> getNonConformitySearchOptionsErrors(ComplianceSearchFilter complianceFilter) {
        if (complianceFilter != null && complianceFilter.getNonConformityOptionsStrings() != null && complianceFilter.getNonConformityOptionsStrings().size() > 0) {
            return complianceFilter.getNonConformityOptionsStrings().stream()
                .filter(option -> !StringUtils.isBlank(option))
                .filter(option -> !isNonConformitySearchOption(option))
                .map(option -> msgUtil.getMessage("search.nonconformitySearchOption.invalid",
                        option,
                        Stream.of(NonConformitySearchOptions.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isNonConformitySearchOption(String option) {
        boolean result = true;
        try {
            NonConformitySearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    private Set<String> getRwtOperatorErrors(SearchRequest searchRequest) {
        if (searchRequest.getRwtOperator() == null
                && !StringUtils.isBlank(searchRequest.getRwtOperatorString())) {
            return Stream.of(msgUtil.getMessage("search.searchOperator.invalid",
                    searchRequest.getRwtOperatorString(),
                    Stream.of(SearchSetOperator.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                    .collect(Collectors.toSet());
        } else if (searchRequest.getRwtOperator() == null
                && StringUtils.isBlank(searchRequest.getRwtOperatorString())
                && hasMultipleRwtOptionsToSearch(searchRequest)) {
            return Stream.of(msgUtil.getMessage("search.rwt.missingSearchOperator")).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean hasMultipleRwtOptionsToSearch(SearchRequest searchRequest) {
        return !CollectionUtils.isEmpty(searchRequest.getRwtOptions())
                && searchRequest.getRwtOptions().size() > 1;
    }

    private Set<String> getRwtOptionsErrors(SearchRequest searchRequest) {
        if (!CollectionUtils.isEmpty(searchRequest.getRwtOptionsStrings())) {
            return searchRequest.getRwtOptionsStrings().stream()
                .filter(option -> !StringUtils.isBlank(option))
                .filter(option -> !isRwtOption(option))
                .map(option -> msgUtil.getMessage("search.rwtOption.invalid",
                        option,
                        Stream.of(RwtSearchOptions.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(","))))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isRwtOption(String option) {
        boolean result = true;
        try {
            RwtSearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
            result = false;
        }
        return result;
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

    private boolean isInSet(Long value, Set<CertificationCriterion> setToSearch) {
        if (setToSearch == null) {
            return false;
        }
        return setToSearch.stream()
            .filter(item -> item.getId().equals(value))
            .count() > 0;
    }

    private boolean isInSet(String value, Set<String> setToSearch) {
        if (setToSearch == null) {
            return false;
        }
        return setToSearch.stream()
            .filter(item -> item.equals(value))
            .count() > 0;
    }
}
