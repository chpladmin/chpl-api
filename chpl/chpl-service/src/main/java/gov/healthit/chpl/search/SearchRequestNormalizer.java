package gov.healthit.chpl.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeCertificationStatuses(request);
        normalizeCertificationEditions(request);
        normalizeCertificationCriterionIds(request);
        normalizeCertificationCriterionOperator(request);
        normalizeCqms(request);
        normalizeCqmsOperator(request);
        normalizeAcbs(request);
        normalizePracticeType(request);
        normalizeCertificationDates(request);
    }

    private void normalizeCertificationStatuses(SearchRequest request) {
        if (request.getCertificationStatuses() != null && request.getCertificationStatuses().size() > 0) {
            request.setCertificationStatuses(request.getCertificationStatuses().stream()
                    .filter(certificationStatus -> !StringUtils.isBlank(certificationStatus))
                    .map(certificationStatus -> certificationStatus.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCertificationEditions(SearchRequest request) {
        if (request.getCertificationEditions() != null && request.getCertificationEditions().size() > 0) {
            request.setCertificationEditions(request.getCertificationEditions().stream()
                    .filter(certificationEdition -> !StringUtils.isBlank(certificationEdition))
                    .map(certificationEdition -> certificationEdition.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCertificationCriterionIds(SearchRequest request) {
        if (request.getCertificationCriteriaIdStrings() != null && request.getCertificationCriteriaIdStrings().size() > 0
                && (request.getCertificationCriteriaIds() == null || request.getCertificationCriteriaIds().size() == 0)) {
            request.setCertificationCriteriaIds(request.getCertificationCriteriaIdStrings().stream()
                    .filter(criterionIdString -> !StringUtils.isBlank(criterionIdString))
                    .map(criterionIdString -> criterionIdString.trim())
                    .filter(criterionIdString -> isParseableLong(criterionIdString))
                    .map(criterionIdString -> Long.parseLong(criterionIdString))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCertificationCriterionOperator(SearchRequest request) {
        if (!StringUtils.isBlank(request.getCertificationCriteriaOperatorString())
                && request.getCertificationCriteriaOperator() == null) {
            try {
                request.setCertificationCriteriaOperator(
                        SearchSetOperator.valueOf(request.getCertificationCriteriaOperatorString().trim()));
            } catch (Exception ignore) {
            }
        }
    }

    private boolean isParseableLong(String value) {
        try {
            Long.parseLong(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private void normalizeCqms(SearchRequest request) {
        if (request.getCqms() != null && request.getCqms().size() > 0) {
            request.setCqms(request.getCqms().stream()
                    .filter(cqm -> !StringUtils.isBlank(cqm))
                    .map(cqm -> cqm.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCqmsOperator(SearchRequest request) {
        if (!StringUtils.isBlank(request.getCqmsOperatorString())
                && request.getCqmsOperator() == null) {
            try {
                request.setCqmsOperator(
                        SearchSetOperator.valueOf(request.getCqmsOperatorString().trim()));
            } catch (Exception ignore) {
            }
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

    private void normalizePracticeType(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getPracticeType())) {
            request.setPracticeType(request.getPracticeType().trim());
        }
    }

    private void normalizeCertificationDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getCertificationDateStart())) {
            request.setCertificationDateStart(request.getCertificationDateStart().trim());
        }
        if (!StringUtils.isEmpty(request.getCertificationDateEnd())) {
            request.setCertificationDateEnd(request.getCertificationDateEnd().trim());
        }
    }
}
