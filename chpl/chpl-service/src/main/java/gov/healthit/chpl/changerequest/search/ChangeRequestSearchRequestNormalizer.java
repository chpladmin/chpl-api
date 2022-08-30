package gov.healthit.chpl.changerequest.search;

import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ChangeRequestSearchRequestNormalizer {

    public void normalize(ChangeRequestSearchRequest request) {
        normalizeDeveloperId(request);
        normalizeStatuses(request);
        normalizeTypes(request);
        normalizeCurrentStatusChangeDates(request);
        normalizeSubmittedDates(request);
        normalizeOrderBy(request);
        normalizePageNumber(request);
        normalizePageSize(request);
    }

    private void normalizeDeveloperId(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getDeveloperIdString())
                && isParseableLong(request.getDeveloperIdString())) {
            request.setDeveloperId(Long.parseLong(request.getDeveloperIdString()));
        }
    }

    private void normalizeStatuses(ChangeRequestSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getCurrentStatusNames())) {
            request.setCurrentStatusNames(request.getCurrentStatusNames().stream()
                    .filter(status -> !StringUtils.isBlank(status))
                    .map(status -> status.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeTypes(ChangeRequestSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getChangeRequestTypeNames())) {
            request.setChangeRequestTypeNames(request.getChangeRequestTypeNames().stream()
                    .filter(type -> !StringUtils.isBlank(type))
                    .map(type -> type.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCurrentStatusChangeDates(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeStart())) {
            request.setCurrentStatusChangeDateTimeStart(request.getCurrentStatusChangeDateTimeStart().trim());
        }
        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateTimeEnd())) {
            request.setCurrentStatusChangeDateTimeEnd(request.getCurrentStatusChangeDateTimeEnd().trim());
        }
    }

    private void normalizeSubmittedDates(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getSubmittedDateTimeStart())) {
            request.setSubmittedDateTimeStart(request.getSubmittedDateTimeStart().trim());
        }
        if (!StringUtils.isEmpty(request.getSubmittedDateTimeEnd())) {
            request.setSubmittedDateTimeEnd(request.getSubmittedDateTimeEnd().trim());
        }
    }

    private void normalizeOrderBy(ChangeRequestSearchRequest request) {
        if (!StringUtils.isBlank(request.getOrderByString())
                && request.getOrderBy() == null) {
            try {
                request.setOrderBy(
                        OrderByOption.valueOf(request.getOrderByString().toUpperCase().trim()));
            } catch (Exception ignore) {
            }
        }
    }

    private void normalizePageNumber(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageNumberString())
                && isParseableInt(request.getPageNumberString())) {
            request.setPageNumber(Integer.parseInt(request.getPageNumberString()));
        }
    }

    private void normalizePageSize(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageSizeString())
                && isParseableInt(request.getPageSizeString())) {
            request.setPageSize(Integer.parseInt(request.getPageSizeString()));
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

    private boolean isParseableInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
