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
        normalizeCreationDates(request);
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
        if (!CollectionUtils.isEmpty(request.getTypeNames())) {
            request.setTypeNames(request.getTypeNames().stream()
                    .filter(type -> !StringUtils.isBlank(type))
                    .map(type -> type.trim())
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCurrentStatusChangeDates(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateStart())) {
            request.setCurrentStatusChangeDateStart(request.getCurrentStatusChangeDateStart().trim());
        }
        if (!StringUtils.isEmpty(request.getCurrentStatusChangeDateEnd())) {
            request.setCurrentStatusChangeDateEnd(request.getCurrentStatusChangeDateEnd().trim());
        }
    }

    private void normalizeCreationDates(ChangeRequestSearchRequest request) {
        if (!StringUtils.isEmpty(request.getCreationDateStart())) {
            request.setCreationDateStart(request.getCreationDateStart().trim());
        }
        if (!StringUtils.isEmpty(request.getCreationDateEnd())) {
            request.setCreationDateEnd(request.getCreationDateEnd().trim());
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
