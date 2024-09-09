package gov.healthit.chpl.activity.search;

import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeConcepts(request);
        normalizeActivityDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeConcepts(SearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getConcepts())) {
            request.setConcepts(request.getConcepts().stream()
                .map(concept -> concept.toUpperCase())
                .collect(Collectors.toSet()));
        }
    }

    private void normalizeActivityDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getActivityDateStart())) {
            request.setActivityDateStart(StringUtils.normalizeSpace(request.getActivityDateStart()));
        } else {
            request.setActivityDateStart(null);
        }
        if (!StringUtils.isEmpty(request.getActivityDateEnd())) {
            request.setActivityDateEnd(StringUtils.normalizeSpace(request.getActivityDateEnd()));
        } else {
            request.setActivityDateEnd(null);
        }
    }

    private void normalizeOrderBy(SearchRequest request) {
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
