package gov.healthit.chpl.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.search.domain.SearchRequest;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeCertificationStatuses(request);
        normalizeCertificationEditions(request);
        normalizeCqms(request);
        normalizeAcbs(request);
        normalizePracticeType(request);
        normalizeCertificationDates(request);
    }

    private void normalizeCertificationStatuses(SearchRequest request) {
        if (request.getCertificationStatuses() != null && request.getCertificationStatuses().size() > 0) {
            request.setCertificationStatuses(request.getCertificationStatuses().stream()
                    .map(certificationStatus -> certificationStatus.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCertificationEditions(SearchRequest request) {
        if (request.getCertificationEditions() != null && request.getCertificationEditions().size() > 0) {
            request.setCertificationEditions(request.getCertificationEditions().stream()
                    .map(certificationEdition -> certificationEdition.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCqms(SearchRequest request) {
        if (request.getCqms() != null && request.getCqms().size() > 0) {
            request.setCqms(request.getCqms().stream()
                    .map(cqm -> cqm.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeAcbs(SearchRequest request) {
        if (request.getCertificationBodies() != null && request.getCertificationBodies().size() > 0) {
            request.setCertificationBodies(request.getCertificationBodies().stream()
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
