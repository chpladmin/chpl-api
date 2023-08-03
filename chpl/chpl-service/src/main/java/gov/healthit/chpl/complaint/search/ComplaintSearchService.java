package gov.healthit.chpl.complaint.search;

import java.time.LocalDate;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ComplaintSearchService {
    private ComplaintDAO complaintDao;
    private ResourcePermissions resourcePermissions;
    private ComplaintSearchRequestValidator validator;
    private ComplaintSearchRequestNormalizer normalizer;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ComplaintSearchService(ComplaintDAO complaintDao,
            ComplaintSearchRequestNormalizer normalizer,
            ComplaintSearchRequestValidator validator,
            ResourcePermissions resourcePermissions) {
        this.complaintDao = complaintDao;
        this.resourcePermissions = resourcePermissions;
        this.validator = validator;
        this.normalizer = normalizer;
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).COMPLAINT, "
            + "T(gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions).SEARCH)")
    public ComplaintSearchResponse searchComplaints(ComplaintSearchRequest searchRequest)
            throws ValidationException {
        normalizer.normalize(searchRequest);
        validator.validate(searchRequest);

        List<Complaint> allComplaints = complaintDao.getAllComplaints();
        List<Complaint> matchedComplaints = filterResults(allComplaints, searchRequest);
        sort(matchedComplaints, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<Complaint> pageOfComplaints
            = getPage(matchedComplaints, getBeginIndex(searchRequest), getEndIndex(searchRequest));

        return ComplaintSearchResponse.builder()
                .results(pageOfComplaints)
                .pageNumber(searchRequest.getPageNumber())
                .pageSize(searchRequest.getPageSize())
                .recordCount(matchedComplaints.size())
                .build();
    }

    private List<Complaint> filterResults(List<Complaint> allComplaints, ComplaintSearchRequest searchRequest) {
        List<CertificationBody> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        List<Long> allowedAcbIds = allowedAcbs.stream().map(acb -> acb.getId()).toList();

        LOGGER.debug("Total complaints: " + allComplaints.size());
        List<Complaint> matchedComplaints = allComplaints.stream()
            .filter(complaint -> matchesAcbIds(complaint, searchRequest.getAcbIds(), allowedAcbIds))
            .filter(complaint -> matchesSearchTerm(complaint, searchRequest.getSearchTerm()))
            .filter(complaint -> matchesInformedOnc(complaint, searchRequest.getInformedOnc()))
            .filter(complaint -> matchesAtlContacted(complaint, searchRequest.getOncAtlContacted()))
            .filter(complaint -> matchesComplainantContacted(complaint, searchRequest.getComplainantContacted()))
            .filter(complaint -> matchesDeveloperContacted(complaint, searchRequest.getDeveloperContacted()))
            .filter(complaint -> matchesComplainantTypes(complaint, searchRequest.getComplainantTypeNames()))
            .filter(complaint -> matchesStatusNames(complaint, searchRequest.getCurrentStatusNames()))
            .filter(complaint -> matchesListingIds(complaint, searchRequest.getListingIds()))
            .filter(complaint -> matchesSurveillanceIds(complaint, searchRequest.getSurveillanceIds()))
            .filter(complaint -> matchesCriteriaIds(complaint, searchRequest.getCertificationCriteriaIds()))
            .filter(complaint -> matchesClosedDateRange(complaint, searchRequest.getClosedDateStart(), searchRequest.getClosedDateEnd()))
            .filter(complaint -> matchesReceivedDateRange(complaint, searchRequest.getReceivedDateStart(), searchRequest.getReceivedDateEnd()))
            .filter(complaint -> matchesOpenDuringDateRange(complaint, searchRequest.getOpenDuringRangeStart(), searchRequest.getOpenDuringRangeEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched complaints: " + matchedComplaints.size());
        return matchedComplaints;
    }

    private boolean matchesAcbIds(Complaint complaint, Set<Long> requestedAcbIds, List<Long> allowedAcbIds) {
        if (CollectionUtils.isEmpty(requestedAcbIds)) {
            return complaint.getCertificationBody() != null
                    && allowedAcbIds.contains(complaint.getCertificationBody().getId());
        }

        return complaint.getCertificationBody() != null
                && allowedAcbIds.contains(complaint.getCertificationBody().getId())
                && requestedAcbIds.contains(complaint.getCertificationBody().getId());
    }

    private boolean matchesSearchTerm(Complaint complaint, String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return true;
        }

        return matchesAcbComplaintId(complaint, searchTerm)
                || matchesOncComplaintId(complaint, searchTerm)
                || matchesAssociatedCertifiedProduct(complaint, searchTerm)
                || matchesAssociatedCriteria(complaint, searchTerm);
    }

    private boolean matchesAcbComplaintId(Complaint complaint, String value) {
        return StringUtils.containsIgnoreCase(complaint.getAcbComplaintId(), value);
    }

    private boolean matchesOncComplaintId(Complaint complaint, String value) {
        return StringUtils.containsIgnoreCase(complaint.getOncComplaintId(), value);
    }

    private boolean matchesAssociatedCertifiedProduct(Complaint complaint, String value) {
        String associatedChplProductNumbers = complaint.getListings().stream()
                .map(listing -> listing.getChplProductNumber())
                .collect(Collectors.joining(" "));
        return StringUtils.containsIgnoreCase(associatedChplProductNumbers, value);
    }

    private boolean matchesAssociatedCriteria(Complaint complaint, String value) {
        String associatedCriteriaNumbers = complaint.getCriteria().stream()
                .map(criterion -> criterion.getCertificationCriterion().getNumber())
                .collect(Collectors.joining(" "));
        return StringUtils.containsIgnoreCase(associatedCriteriaNumbers, value);
    }

    private boolean matchesInformedOnc(Complaint complaint, Set<Boolean> values) {
        if (CollectionUtils.isEmpty(values)) {
            return true;
        }
        return values.stream()
                .filter(value -> value.booleanValue() == complaint.isFlagForOncReview())
                .findAny().isPresent();

    }

    private boolean matchesAtlContacted(Complaint complaint, Set<Boolean> values) {
        if (CollectionUtils.isEmpty(values)) {
            return true;
        }
        return values.stream()
                .filter(value -> value.booleanValue() == complaint.isOncAtlContacted())
                .findAny().isPresent();
    }

    private boolean matchesComplainantContacted(Complaint complaint, Set<Boolean> values) {
        if (CollectionUtils.isEmpty(values)) {
            return true;
        }
        return values.stream()
                .filter(value -> value.booleanValue() == complaint.isComplainantContacted())
                .findAny().isPresent();
    }

    private boolean matchesDeveloperContacted(Complaint complaint, Set<Boolean> values) {
        if (CollectionUtils.isEmpty(values)) {
            return true;
        }
        return values.stream()
                .filter(value -> value.booleanValue() == complaint.isDeveloperContacted())
                .findAny().isPresent();
    }

    private boolean matchesComplainantTypes(Complaint complaint, Set<String> complainantTypeNames) {
        if (CollectionUtils.isEmpty(complainantTypeNames)) {
            return true;
        }

        Set<String> complainantTypeNamesUpperCase = complainantTypeNames.stream().map(ctn -> ctn.toUpperCase()).collect(Collectors.toSet());
        return matchesComplainantTypeName(complaint, complainantTypeNamesUpperCase)
                || matchesComplainantTypeNameOther(complaint, complainantTypeNamesUpperCase);
    }

    private boolean matchesComplainantTypeName(Complaint complaint, Set<String> complainantTypeNames) {
        return complaint.getComplainantType() != null
                && !StringUtils.isBlank(complaint.getComplainantType().getName())
                && complainantTypeNames.contains(complaint.getComplainantType().getName().toUpperCase());
    }

    private boolean matchesComplainantTypeNameOther(Complaint complaint, Set<String> complainantTypeNames) {
        return !StringUtils.isBlank(complaint.getComplainantTypeOther())
                    && complainantTypeNames.contains(complaint.getComplainantTypeOther().toUpperCase());
    }

    private boolean matchesStatusNames(Complaint complaint, Set<String> statusNames) {
        if (CollectionUtils.isEmpty(statusNames)) {
            return true;
        }

        List<String> statusNamesUpperCase = statusNames.stream().map(sn -> sn.toUpperCase()).collect(Collectors.toList());
        return (statusNamesUpperCase.contains(Complaint.COMPLAINT_OPEN.toUpperCase()) && complaint.getClosedDate() == null)
                || (statusNamesUpperCase.contains(Complaint.COMPLAINT_CLOSED.toUpperCase()) && complaint.getClosedDate() != null);
    }

    private boolean matchesListingIds(Complaint complaint, Set<Long> searchListingIds) {
        if (CollectionUtils.isEmpty(searchListingIds)) {
            return true;
        } else if (CollectionUtils.isEmpty(complaint.getListings())) {
            return false;
        }

        List<Long> complaintListingIds = complaint.getListings().stream().map(map -> map.getListingId()).toList();
        return CollectionUtils.containsAny(searchListingIds, complaintListingIds);
    }

    private boolean matchesSurveillanceIds(Complaint complaint, Set<Long> searchSurveillanceIds) {
        if (CollectionUtils.isEmpty(searchSurveillanceIds)) {
            return true;
        } else if (CollectionUtils.isEmpty(complaint.getSurveillances())) {
            return false;
        }

        List<Long> complaintSurveillanceIds = complaint.getSurveillances().stream().map(map -> map.getSurveillanceId()).toList();
        return CollectionUtils.containsAny(searchSurveillanceIds, complaintSurveillanceIds);
    }

    private boolean matchesCriteriaIds(Complaint complaint, Set<Long> searchCriteriaIds) {
        if (CollectionUtils.isEmpty(searchCriteriaIds)) {
            return true;
        } else if (CollectionUtils.isEmpty(complaint.getCriteria())) {
            return false;
        }

        List<Long> complaintCriteriaIds = complaint.getCriteria().stream().map(map -> map.getCertificationCriterionId()).toList();
        return CollectionUtils.containsAny(searchCriteriaIds, complaintCriteriaIds);
    }

    private boolean matchesClosedDateRange(Complaint complaint, String rangeStart, String rangeEnd) {
        if (StringUtils.isAllEmpty(rangeStart, rangeEnd)) {
            return true;
        }
        LocalDate startDate = parseLocalDate(rangeStart);
        LocalDate endDate = parseLocalDate(rangeEnd);
        if (complaint.getClosedDate() != null) {
            if (startDate == null && endDate != null) {
                return complaint.getClosedDate().isEqual(endDate) || complaint.getClosedDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return complaint.getClosedDate().isEqual(startDate) || complaint.getClosedDate().isAfter(startDate);
            } else {
                return (complaint.getClosedDate().isEqual(endDate) || complaint.getClosedDate().isBefore(endDate))
                      && (complaint.getClosedDate().isEqual(startDate) || complaint.getClosedDate().isAfter(startDate));
            }
        }
        return false;
    }

    private boolean matchesReceivedDateRange(Complaint complaint, String rangeStart, String rangeEnd) {
        if (StringUtils.isAllEmpty(rangeStart, rangeEnd)) {
            return true;
        }
        LocalDate startDate = parseLocalDate(rangeStart);
        LocalDate endDate = parseLocalDate(rangeEnd);
        if (complaint.getReceivedDate() != null) {
            if (startDate == null && endDate != null) {
                return complaint.getReceivedDate().isEqual(endDate) || complaint.getReceivedDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return complaint.getReceivedDate().isEqual(startDate) || complaint.getReceivedDate().isAfter(startDate);
            } else {
                return (complaint.getReceivedDate().isEqual(endDate) || complaint.getReceivedDate().isBefore(endDate))
                      && (complaint.getReceivedDate().isEqual(startDate) || complaint.getReceivedDate().isAfter(startDate));
            }
        }
        return false;
    }

    private boolean matchesOpenDuringDateRange(Complaint complaint, String rangeStart, String rangeEnd) {
        if (StringUtils.isAnyEmpty(rangeStart, rangeEnd)) {
            return true;
        }
        LocalDate searchStartDate = parseLocalDate(rangeStart);
        LocalDate searchEndDate = parseLocalDate(rangeEnd);
        LocalDate complaintOpenDate = complaint.getReceivedDate();
        LocalDate complaintClosedDate = complaint.getClosedDate();

        return (complaintOpenDate.isEqual(searchEndDate)
                    || complaintOpenDate.isBefore(searchEndDate))
                &&
                (complaintClosedDate == null
                    || complaintClosedDate.isEqual(searchStartDate)
                    || complaintClosedDate.isAfter(searchStartDate));
    }

    private LocalDate parseLocalDate(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }

        LocalDate date = null;
        try {
            date = LocalDate.parse(dateString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + dateString + " as LocalDate of the format " + ComplaintSearchRequest.DATE_SEARCH_FORMAT);
        }
        return date;
    }

    private List<Complaint> getPage(List<Complaint> complaints, int beginIndex, int endIndex) {
        if (endIndex > complaints.size()) {
            endIndex = complaints.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<Complaint>();
        }
        LOGGER.debug("Getting filtered complaints between [" + beginIndex + ", " + endIndex + ")");
        return complaints.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(ComplaintSearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(ComplaintSearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<Complaint> complaints, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case ACB_COMPLAINT_ID:
                complaints.sort(new AcbComplaintIdComparator(descending));
                break;
            case ONC_COMPLAINT_ID:
                complaints.sort(new OncComplaintIdComparator(descending));
                break;
            case CERTIFICATION_BODY:
                complaints.sort(new CertificationBodyComparator(descending));
                break;
            case COMPLAINANT_TYPE:
                complaints.sort(new ComplainantTypeComparator(descending));
                break;
            case CURRENT_STATUS:
                complaints.sort(new ComplaintStatusComparator(descending));
                break;
            case RECEIVED_DATE:
                complaints.sort(new ReceivedDateComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class AcbComplaintIdComparator implements Comparator<Complaint> {
        private boolean descending = false;

        AcbComplaintIdComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            int sortFactor = descending ? -1 : 1;
            return StringUtils.compare(complaint1.getAcbComplaintId(), complaint2.getAcbComplaintId()) * sortFactor;
        }
    }

    private class OncComplaintIdComparator implements Comparator<Complaint> {
        private boolean descending = false;

        OncComplaintIdComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            int sortFactor = descending ? -1 : 1;
            return StringUtils.compare(complaint1.getOncComplaintId(), complaint2.getOncComplaintId()) * sortFactor;
        }
    }

    private class CertificationBodyComparator implements Comparator<Complaint> {
        private boolean descending = false;

        CertificationBodyComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            if (complaint1.getCertificationBody() == null || complaint2.getCertificationBody() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (StringUtils.compare(complaint1.getCertificationBody().getName(),
                    complaint2.getCertificationBody().getName())) * sortFactor;
        }
    }

    private class ComplainantTypeComparator implements Comparator<Complaint> {
        private boolean descending = false;

        ComplainantTypeComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            String complainantType1 = complaint1.getComplainantType() == null ? complaint1.getComplainantTypeOther()
                       : complaint1.getComplainantType().getName();
            String complainantType2 = complaint2.getComplainantType() == null ? complaint2.getComplainantTypeOther()
                    : complaint2.getComplainantType().getName();
            int sortFactor = descending ? -1 : 1;
            return (StringUtils.compareIgnoreCase(complainantType1, complainantType2)) * sortFactor;
        }
    }

    private class ComplaintStatusComparator implements Comparator<Complaint> {
        private boolean descending = false;

        ComplaintStatusComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            String complaint1Status = complaint1.getClosedDate() == null ? Complaint.COMPLAINT_OPEN : Complaint.COMPLAINT_CLOSED;
            String complaint2Status = complaint2.getClosedDate() == null ? Complaint.COMPLAINT_OPEN : Complaint.COMPLAINT_CLOSED;

            int sortFactor = descending ? -1 : 1;
            return (complaint1Status.compareTo(complaint2Status)) * sortFactor;
        }
    }

    private class ReceivedDateComparator implements Comparator<Complaint> {
        private boolean descending = false;

        ReceivedDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(Complaint complaint1, Complaint complaint2) {
            int sortFactor = descending ? -1 : 1;
            return (ObjectUtils.compare(complaint1.getReceivedDate(), complaint2.getReceivedDate())) * sortFactor;
        }
    }
}
