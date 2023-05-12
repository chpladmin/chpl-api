package gov.healthit.chpl.questionableactivity.search;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


public class SearchRequestNormalizer {

    public void normalize(SearchRequest request) {
        normalizeSearchTerm(request);
        normalizeTriggerIds(request);
        normalizeActivityDates(request);
        normalizeOrderBy(request);
    }

    private void normalizeSearchTerm(SearchRequest request) {
        if (!StringUtils.isEmpty(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        }
    }

    private void normalizeTriggerIds(SearchRequest request) {
        if (request.getTriggerIdStrings() != null && request.getTriggerIdStrings().size() > 0
                && (request.getTriggerIds() == null || request.getTriggerIds().size() == 0)) {
            request.setTriggerIds(request.getTriggerIdStrings().stream()
                    .filter(triggerIdString -> !StringUtils.isBlank(triggerIdString))
                    .map(triggerIdString -> triggerIdString.trim())
                    .filter(triggerIdString -> isParseableLong(triggerIdString))
                    .map(triggerIdString -> Long.parseLong(triggerIdString))
                    .collect(Collectors.toSet()));
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
