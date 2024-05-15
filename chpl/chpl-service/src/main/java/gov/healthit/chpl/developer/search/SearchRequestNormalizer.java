package gov.healthit.chpl.developer.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.search.domain.SearchSetOperator;

public class SearchRequestNormalizer {

    public void normalize(DeveloperSearchRequest request) {
        normalizeSearchTerm(request);
        normalizeDeveloperName(request);
        normalizeDeveloperCode(request);
        normalizeStatuses(request);
        normalizeAcbsForActiveListings(request);
        normalizeAcbsForAllListings(request);
        normalizeDecertificationDates(request);
        normalizeActiveListingsFilter(request);
        normalizeAttestationsFilter(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(DeveloperSearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        }
    }

    private void normalizeDeveloperName(DeveloperSearchRequest request) {
        if (!StringUtils.isEmpty(request.getDeveloperName())) {
            request.setDeveloperName(StringUtils.normalizeSpace(request.getDeveloperName()));
        }
    }

    private void normalizeDeveloperCode(DeveloperSearchRequest request) {
        if (!StringUtils.isEmpty(request.getDeveloperCode())) {
            request.setDeveloperCode(StringUtils.normalizeSpace(request.getDeveloperCode()));
        }
    }

    private void normalizeStatuses(DeveloperSearchRequest request) {
        if (request.getStatuses() != null && request.getStatuses().size() > 0) {
            request.setStatuses(request.getStatuses().stream()
                    .filter(status -> !StringUtils.isBlank(status))
                    .map(status -> StringUtils.normalizeSpace(status))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeAcbsForActiveListings(DeveloperSearchRequest request) {
        if (request.getAcbsForActiveListings() != null && request.getAcbsForActiveListings().size() > 0) {
            request.setAcbsForActiveListings(request.getAcbsForActiveListings().stream()
                    .filter(acb -> !StringUtils.isBlank(acb))
                    .map(acb -> StringUtils.normalizeSpace(acb))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeAcbsForAllListings(DeveloperSearchRequest request) {
        if (request.getAcbsForAllListings() != null && request.getAcbsForAllListings().size() > 0) {
            request.setAcbsForAllListings(request.getAcbsForAllListings().stream()
                    .filter(acb -> !StringUtils.isBlank(acb))
                    .map(acb -> StringUtils.normalizeSpace(acb))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeDecertificationDates(DeveloperSearchRequest request) {
        if (!StringUtils.isEmpty(request.getDecertificationDateStart())) {
            request.setDecertificationDateStart(StringUtils.normalizeSpace(request.getDecertificationDateStart()));
        }
        if (!StringUtils.isEmpty(request.getDecertificationDateEnd())) {
            request.setDecertificationDateEnd(StringUtils.normalizeSpace(request.getDecertificationDateEnd()));
        }
    }

    private void normalizeActiveListingsFilter(DeveloperSearchRequest request) {
        normalizeActiveListingOptions(request);
        normalizeActiveListingOptionsOperator(request);
    }

    private void normalizeActiveListingOptions(DeveloperSearchRequest request) {
        if (request.getActiveListingsOptionsStrings() != null
                && request.getActiveListingsOptionsStrings().size() > 0
                && (request.getActiveListingsOptions() == null || request.getActiveListingsOptions().size() == 0)) {
            try {
                request.setActiveListingsOptions(
                        request.getActiveListingsOptionsStrings().stream()
                            .filter(option -> !StringUtils.isBlank(option))
                            .map(option -> convertToActiveListingSearchOption(option))
                            .filter(option -> option != null)
                            .collect(Collectors.toSet()));
            } catch (Exception ignore) {
            }
        }
    }

    private ActiveListingSearchOptions convertToActiveListingSearchOption(String option) {
        if (StringUtils.isBlank(option)) {
            return null;
        }
        ActiveListingSearchOptions convertedOption = null;
        try {
            convertedOption = ActiveListingSearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
        }
        return convertedOption;
    }

    private void normalizeActiveListingOptionsOperator(DeveloperSearchRequest request) {
        if (!StringUtils.isBlank(request.getActiveListingsOptionsOperatorString())
                && request.getActiveListingsOptionsOperator() == null) {
            try {
                request.setActiveListingsOptionsOperator(
                        SearchSetOperator.valueOf(request.getActiveListingsOptionsOperatorString().toUpperCase().trim()));
            } catch (Exception ignore) {
            }
        }
    }

    private void normalizeAttestationsFilter(DeveloperSearchRequest request) {
        normalizeAttestationsOptions(request);
        normalizeAttestationsOptionsOperator(request);
    }

    private void normalizeAttestationsOptions(DeveloperSearchRequest request) {
        if (request.getAttestationsOptionsStrings() != null
                && request.getAttestationsOptionsStrings().size() > 0
                && (request.getAttestationsOptions() == null || request.getAttestationsOptions().size() == 0)) {
            try {
                request.setAttestationsOptions(
                        request.getAttestationsOptionsStrings().stream()
                            .filter(option -> !StringUtils.isBlank(option))
                            .map(option -> convertToAttestationsSearchOption(option))
                            .filter(option -> option != null)
                            .collect(Collectors.toSet()));
            } catch (Exception ignore) {
            }
        }
    }

    private AttestationsSearchOptions convertToAttestationsSearchOption(String option) {
        if (StringUtils.isBlank(option)) {
            return null;
        }
        AttestationsSearchOptions convertedOption = null;
        try {
            convertedOption = AttestationsSearchOptions.valueOf(option.toUpperCase().trim());
        } catch (Exception ex) {
        }
        return convertedOption;
    }

    private void normalizeAttestationsOptionsOperator(DeveloperSearchRequest request) {
        if (!StringUtils.isBlank(request.getAttestationsOptionsOperatorString())
                && request.getAttestationsOptionsOperator() == null) {
            try {
                request.setAttestationsOptionsOperator(
                        SearchSetOperator.valueOf(request.getAttestationsOptionsOperatorString().toUpperCase().trim()));
            } catch (Exception ignore) {
            }
        }
    }

    private void normalizeOrderBy(DeveloperSearchRequest request) {
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
