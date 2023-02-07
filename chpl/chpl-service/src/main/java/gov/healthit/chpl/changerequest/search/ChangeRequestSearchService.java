package gov.healthit.chpl.changerequest.search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestSearchService {
    private ChangeRequestDAO changeRequestDAO;
    private ResourcePermissions resourcePermissions;
    private ChangeRequestSearchRequestValidator validator;
    private ChangeRequestSearchRequestNormalizer normalizer;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ChangeRequestSearchService(ChangeRequestDAO changeRequestDAO,
            ChangeRequestSearchRequestValidator validator,
            ResourcePermissions resourcePermissions) {
        this.changeRequestDAO = changeRequestDAO;
        this.resourcePermissions = resourcePermissions;
        this.validator = validator;
        this.normalizer = new ChangeRequestSearchRequestNormalizer();
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).SEARCH)")
    public ChangeRequestSearchResponse searchChangeRequests(ChangeRequestSearchRequest searchRequest)
            throws ValidationException {
        normalizer.normalize(searchRequest);
        validator.validate(searchRequest);

        List<ChangeRequestSearchResult> allChangeRequestsForUser = new ArrayList<ChangeRequestSearchResult>();
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            allChangeRequestsForUser = changeRequestDAO.getAllForAcbs(resourcePermissions.getAllAcbsForCurrentUser().stream()
                    .map(acb -> acb.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            allChangeRequestsForUser = changeRequestDAO.getAllForDevelopers(resourcePermissions.getAllDevelopersForCurrentUser().stream()
                    .map(dev -> dev.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleOnc() || resourcePermissions.isUserRoleAdmin()) {
            allChangeRequestsForUser = changeRequestDAO.getAll();
        }

        List<ChangeRequestSearchResult> matchedChangeRequests = filterResults(allChangeRequestsForUser, searchRequest);
        sort(matchedChangeRequests, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<ChangeRequestSearchResult> pageOfChangeRequests
            = getPage(matchedChangeRequests, getBeginIndex(searchRequest), getEndIndex(searchRequest));

        return ChangeRequestSearchResponse.builder()
                .results(pageOfChangeRequests)
                .pageNumber(searchRequest.getPageNumber())
                .pageSize(searchRequest.getPageSize())
                .recordCount(matchedChangeRequests.size())
                .build();
    }

    private List<ChangeRequestSearchResult> filterResults(List<ChangeRequestSearchResult> allChangeRequests,
            ChangeRequestSearchRequest searchRequest) {
        LOGGER.debug("Total change requests: " + allChangeRequests.size());
        List<ChangeRequestSearchResult> matchedChangeRequests = allChangeRequests.stream()
            .filter(changeRequest -> matchesSearchTerm(changeRequest, searchRequest.getSearchTerm()))
            .filter(changeRequest -> matchesDeveloperId(changeRequest, searchRequest.getDeveloperId()))
            .filter(changeRequest -> matchesAcbIds(changeRequest, searchRequest.getAcbIds()))
            .filter(changeRequest -> matchesStatusNames(changeRequest, searchRequest.getCurrentStatusNames()))
            .filter(changeRequest -> matchesTypeNames(changeRequest, searchRequest.getChangeRequestTypeNames()))
            .filter(changeRequest -> matchesCurrentStatusChangeDateTimeRange(changeRequest, searchRequest.getCurrentStatusChangeDateTimeStart(), searchRequest.getCurrentStatusChangeDateTimeEnd()))
            .filter(changeRequest -> matchesSubmittedDateTimeRange(changeRequest, searchRequest.getSubmittedDateTimeStart(), searchRequest.getSubmittedDateTimeEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched change requests: " + matchedChangeRequests.size());
        return matchedChangeRequests;
    }

    private boolean matchesSearchTerm(ChangeRequestSearchResult changeRequest, String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return true;
        }

        String searchTermUpperCase = searchTerm.toUpperCase();
        return changeRequest.getDeveloper() != null
                && !StringUtils.isEmpty(changeRequest.getDeveloper().getName())
                && changeRequest.getDeveloper().getName().toUpperCase().contains(searchTermUpperCase);
    }

    private boolean matchesDeveloperId(ChangeRequestSearchResult changeRequest, Long developerId) {
        if (developerId == null) {
            return true;
        }

        return changeRequest.getDeveloper() != null
                && changeRequest.getDeveloper().getId() != null
                && changeRequest.getDeveloper().getId().equals(developerId);
    }

    private boolean matchesAcbIds(ChangeRequestSearchResult changeRequest, Set<Long> acbIds) {
        if (CollectionUtils.isEmpty(acbIds)) {
            return true;
        }

        return !CollectionUtils.isEmpty(changeRequest.getCertificationBodies())
                && changeRequest.getCertificationBodies().stream()
                    .map(crAcbs -> crAcbs.getId())
                    .filter(crAcbId -> acbIds.contains(crAcbId))
                    .findAny().isPresent();
    }

    private boolean matchesStatusNames(ChangeRequestSearchResult changeRequest, Set<String> statusNames) {
        if (CollectionUtils.isEmpty(statusNames)) {
            return true;
        }

        List<String> statusNamesUpperCase = statusNames.stream().map(status -> status.toUpperCase()).collect(Collectors.toList());
        return changeRequest.getCurrentStatus() != null
                && !StringUtils.isEmpty(changeRequest.getCurrentStatus().getName())
                && statusNamesUpperCase.contains(changeRequest.getCurrentStatus().getName().toUpperCase());
    }

    private boolean matchesTypeNames(ChangeRequestSearchResult changeRequest, Set<String> typeNames) {
        if (CollectionUtils.isEmpty(typeNames)) {
            return true;
        }

        List<String> typeNamesUpperCase = typeNames.stream().map(type -> type.toUpperCase()).collect(Collectors.toList());
        return changeRequest.getChangeRequestType() != null
                && !StringUtils.isEmpty(changeRequest.getChangeRequestType().getName())
                && typeNamesUpperCase.contains(changeRequest.getChangeRequestType().getName().toUpperCase());
    }

    private boolean matchesCurrentStatusChangeDateTimeRange(ChangeRequestSearchResult changeRequest, String rangeStart,
            String rangeEnd) {
        if (StringUtils.isAllEmpty(rangeStart, rangeEnd)) {
            return true;
        }
        LocalDateTime startDateTime = parseLocalDateTime(rangeStart);
        LocalDateTime endDateTime = parseLocalDateTime(rangeEnd);
        if (changeRequest.getCurrentStatus() != null
                && changeRequest.getCurrentStatus().getStatusChangeDateTime() != null) {
            if (startDateTime == null && endDateTime != null) {
                return changeRequest.getCurrentStatus().getStatusChangeDateTime().isEqual(endDateTime)
                        || changeRequest.getCurrentStatus().getStatusChangeDateTime().isBefore(endDateTime);
            } else if (startDateTime != null && endDateTime == null) {
                return changeRequest.getCurrentStatus().getStatusChangeDateTime().isEqual(startDateTime)
                        || changeRequest.getCurrentStatus().getStatusChangeDateTime().isAfter(startDateTime);
            } else {
                return (changeRequest.getCurrentStatus().getStatusChangeDateTime().isEqual(endDateTime)
                        || changeRequest.getCurrentStatus().getStatusChangeDateTime().isBefore(endDateTime))
                       && (changeRequest.getCurrentStatus().getStatusChangeDateTime().isEqual(startDateTime)
                        || changeRequest.getCurrentStatus().getStatusChangeDateTime().isAfter(startDateTime));
            }
        }
        return false;
    }

    private boolean matchesSubmittedDateTimeRange(ChangeRequestSearchResult changeRequest, String rangeStart,
            String rangeEnd) {
        if (StringUtils.isAllEmpty(rangeStart, rangeEnd)) {
            return true;
        }
        LocalDateTime startDateTime = parseLocalDateTime(rangeStart);
        LocalDateTime endDateTime = parseLocalDateTime(rangeEnd);
        if (changeRequest.getSubmittedDateTime() != null
                && changeRequest.getSubmittedDateTime() != null) {
            if (startDateTime == null && endDateTime != null) {
                return changeRequest.getSubmittedDateTime().isEqual(endDateTime)
                        || changeRequest.getSubmittedDateTime().isBefore(endDateTime);
            } else if (startDateTime != null && endDateTime == null) {
                return changeRequest.getSubmittedDateTime().isEqual(startDateTime)
                        || changeRequest.getSubmittedDateTime().isAfter(startDateTime);
            } else {
                return (changeRequest.getSubmittedDateTime().isEqual(endDateTime)
                        || changeRequest.getSubmittedDateTime().isBefore(endDateTime))
                      && (changeRequest.getSubmittedDateTime().isEqual(startDateTime)
                        || changeRequest.getSubmittedDateTime().isAfter(startDateTime));
            }
        }
        return false;
    }

    private LocalDateTime parseLocalDateTime(String timestampString) {
        if (StringUtils.isEmpty(timestampString)) {
            return null;
        }

        LocalDateTime date = null;
        try {
            date = LocalDateTime.parse(timestampString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + timestampString + " as LocalDateTime of the format " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT);
        }
        return date;
    }

    private List<ChangeRequestSearchResult> getPage(List<ChangeRequestSearchResult> changeRequests, int beginIndex, int endIndex) {
        if (endIndex > changeRequests.size()) {
            endIndex = changeRequests.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<ChangeRequestSearchResult>();
        }
        LOGGER.debug("Getting filtered change request results between [" + beginIndex + ", " + endIndex + ")");
        return changeRequests.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(ChangeRequestSearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(ChangeRequestSearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<ChangeRequestSearchResult> changeRequests, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case DEVELOPER:
                changeRequests.sort(new DeveloperComparator(descending));
                break;
            case CERTIFICATION_BODIES:
                changeRequests.sort(new CertificationBodiesComparator(descending));
                break;
            case CHANGE_REQUEST_STATUS:
                changeRequests.sort(new ChangeRequestStatusComparator(descending));
                break;
            case CHANGE_REQUEST_TYPE:
                changeRequests.sort(new ChangeRequestTypeComparator(descending));
                break;
            case SUBMITTED_DATE_TIME:
                changeRequests.sort(new SubmittedDateTimeComparator(descending));
                break;
            case CURRENT_STATUS_CHANGE_DATE_TIME:
                changeRequests.sort(new CurrentStatusChangeDateTimeComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class DeveloperComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        DeveloperComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (ObjectUtils.anyNull(changeRequest1.getDeveloper(), changeRequest2.getDeveloper())
                    || StringUtils.isAnyEmpty(changeRequest1.getDeveloper().getName(), changeRequest2.getDeveloper().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1.getDeveloper().getName().compareTo(changeRequest2.getDeveloper().getName())) * sortFactor;
        }
    }

    private class CertificationBodiesComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        CertificationBodiesComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (CollectionUtils.isEmpty(changeRequest1.getCertificationBodies())
                    || CollectionUtils.isEmpty(changeRequest2.getCertificationBodies())
                    || StringUtils.isAnyEmpty(changeRequest1.getCertificationBodies().stream().map(acb -> acb.getName()).toList().toArray(new String[0]))
                    || StringUtils.isAnyEmpty(changeRequest2.getCertificationBodies().stream().map(acb -> acb.getName()).toList().toArray(new String[0]))) {
                return 0;
            }
            changeRequest1.getCertificationBodies().sort(new IdNamePairComparator());
            changeRequest2.getCertificationBodies().sort(new IdNamePairComparator());
            String changeRequest1SmushedAcbs = changeRequest1.getCertificationBodies().stream()
                    .map(acb -> acb.getName())
                    .collect(Collectors.joining(","));
            String changeRequest2SmushedAcbs = changeRequest2.getCertificationBodies().stream()
                    .map(acb -> acb.getName())
                    .collect(Collectors.joining(","));
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1SmushedAcbs.compareTo(changeRequest2SmushedAcbs)) * sortFactor;
        }
    }

    private class ChangeRequestStatusComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        ChangeRequestStatusComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (ObjectUtils.anyNull(changeRequest1.getCurrentStatus(), changeRequest2.getCurrentStatus())
                    || StringUtils.isAnyEmpty(changeRequest1.getCurrentStatus().getName(), changeRequest2.getCurrentStatus().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1.getCurrentStatus().getName().compareTo(changeRequest2.getCurrentStatus().getName())) * sortFactor;
        }
    }

    private class ChangeRequestTypeComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        ChangeRequestTypeComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (ObjectUtils.anyNull(changeRequest1.getChangeRequestType(), changeRequest2.getChangeRequestType())
                    || StringUtils.isAnyEmpty(changeRequest1.getChangeRequestType().getName(), changeRequest2.getChangeRequestType().getName())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1.getChangeRequestType().getName().compareTo(changeRequest2.getChangeRequestType().getName())) * sortFactor;
        }
    }

    private class SubmittedDateTimeComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        SubmittedDateTimeComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (ObjectUtils.anyNull(changeRequest1.getSubmittedDateTime(), changeRequest2.getSubmittedDateTime())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1.getSubmittedDateTime().compareTo(changeRequest2.getSubmittedDateTime())) * sortFactor;
        }
    }

    private class CurrentStatusChangeDateTimeComparator implements Comparator<ChangeRequestSearchResult> {
        private boolean descending = false;

        CurrentStatusChangeDateTimeComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(ChangeRequestSearchResult changeRequest1, ChangeRequestSearchResult changeRequest2) {
            if (ObjectUtils.anyNull(changeRequest1.getCurrentStatus(), changeRequest2.getCurrentStatus(),
                    changeRequest1.getCurrentStatus().getStatusChangeDateTime(),
                    changeRequest2.getCurrentStatus().getStatusChangeDateTime())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (changeRequest1.getCurrentStatus().getStatusChangeDateTime()
                    .compareTo(changeRequest2.getCurrentStatus().getStatusChangeDateTime())) * sortFactor;
        }
    }

    private class IdNamePairComparator implements Comparator<IdNamePair> {
        IdNamePairComparator() {
        }

        @Override
        public int compare(IdNamePair item1, IdNamePair item2) {
            if (ObjectUtils.anyNull(item1.getName(), item2.getName())) {
                return 0;
            }
            return item1.getName().compareTo(item2.getName());
        }
    }
}
