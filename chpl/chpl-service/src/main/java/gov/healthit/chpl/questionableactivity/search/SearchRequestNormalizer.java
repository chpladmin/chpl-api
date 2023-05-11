package gov.healthit.chpl.questionableactivity.search;

import org.apache.commons.lang3.StringUtils;

public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeSearchTerm(request);
        normalizeActivityDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        }
    }

    private void normalizeActivityDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getActivityDateStart())) {
            request.setActivityDateStart(StringUtils.normalizeSpace(request.getActivityDateStart()));
        }
        if (!StringUtils.isEmpty(request.getActivityDateEnd())) {
            request.setActivityDateEnd(StringUtils.normalizeSpace(request.getActivityDateEnd()));
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
