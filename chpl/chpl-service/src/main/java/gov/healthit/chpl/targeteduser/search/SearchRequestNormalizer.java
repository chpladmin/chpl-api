package gov.healthit.chpl.targeteduser.search;

import org.apache.commons.lang3.StringUtils;


public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeSearchTerm(request);
        normalizeIsUsed(request);
        normalizeCreationDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        }
    }

    private void normalizeIsUsed(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getIsUsed())) {
            request.setIsUsed(StringUtils.normalizeSpace(request.getIsUsed()));
        } else {
            request.setIsUsed(null);
        }
    }

    private void normalizeCreationDates(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getCreationDateStart())) {
            request.setCreationDateStart(StringUtils.normalizeSpace(request.getCreationDateStart()));
        }
        if (!StringUtils.isEmpty(request.getCreationDateEnd())) {
            request.setCreationDateEnd(StringUtils.normalizeSpace(request.getCreationDateEnd()));
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
