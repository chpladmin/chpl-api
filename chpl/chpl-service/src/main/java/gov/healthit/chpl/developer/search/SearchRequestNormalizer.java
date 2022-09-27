package gov.healthit.chpl.developer.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeSearchTerm(request);
        normalizeDeveloperName(request);
        normalizeDeveloperCode(request);
        normalizeStatuses(request);
        normalizeAcbs(request);
        normalizeDecertificationDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(request.getSearchTerm().trim());
        }
    }

    private void normalizeDeveloperName(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getDeveloperName())) {
            request.setDeveloperName(request.getDeveloperName().trim());
        }
    }

    private void normalizeDeveloperCode(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getDeveloperCode())) {
            request.setDeveloperCode(request.getDeveloperCode().trim());
        }
    }

    private void normalizeStatuses(SearchRequest request) {
        if (request.getStatuses() != null && request.getStatuses().size() > 0) {
            request.setStatuses(request.getStatuses().stream()
                    .filter(status -> !StringUtils.isBlank(status))
                    .map(status -> status.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeAcbs(SearchRequest request) {
        if (request.getCertificationBodies() != null && request.getCertificationBodies().size() > 0) {
            request.setCertificationBodies(request.getCertificationBodies().stream()
                    .filter(acb -> !StringUtils.isBlank(acb))
                    .map(acb -> acb.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeDecertificationDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getDecertificationDateStart())) {
            request.setDecertificationDateStart(request.getDecertificationDateStart().trim());
        }
        if (!StringUtils.isEmpty(request.getDecertificationDateEnd())) {
            request.setDecertificationDateEnd(request.getDecertificationDateEnd().trim());
        }
    }

    private void normalizeOrderBy(SearchRequest request) {
        if (!StringUtils.isBlank(request.getOrderByString())
                && request.getOrderBy() == null) {
            try {
                request.setOrderBy(
                        OrderByOption.valueOf(request.getOrderByString().toUpperCase().trim()));
            } catch (Exception ignore) {
            }
        }
    }
}
