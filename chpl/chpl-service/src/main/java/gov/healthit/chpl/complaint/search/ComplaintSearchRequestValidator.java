package gov.healthit.chpl.complaint.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ComplaintSearchRequestValidator {
    private ComplaintDAO complaintDao;
    private CertificationBodyDAO acbDao;
    private ErrorMessageUtil msgUtil;
    private DateTimeFormatter dateFormatter;

    private Set<String> complaintStatuses;

    @Autowired
    public ComplaintSearchRequestValidator(ComplaintDAO complaintDao,
            CertificationBodyDAO acbDao,
            ErrorMessageUtil msgUtil) {
        this.complaintDao = complaintDao;
        this.acbDao = acbDao;
        this.msgUtil = msgUtil;
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        complaintStatuses = Stream.of(Complaint.COMPLAINT_OPEN, Complaint.COMPLAINT_CLOSED)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void validate(ComplaintSearchRequest request) throws ValidationException {
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(getInformedOncErrors(request));
        errors.addAll(getAtlContactedErrors(request));
        errors.addAll(getComplainantContactedErrors(request));
        errors.addAll(getDeveloperContactedErrors(request));
        errors.addAll(getAcbIdsErrors(request));
        errors.addAll(getComplainantTypeNameErrors(request));
        errors.addAll(getCurrentStatusErrors(request));
        errors.addAll(getListingIdFormatErrors(request.getListingIdStrings()));
        errors.addAll(getSurveillanceIdFormatErrors(request.getSurveillanceIdStrings()));
        errors.addAll(getCriteriaIdFormatErrors(request.getCertificationCriteriaIdStrings()));
        errors.addAll(getClosedDateErrors(request));
        errors.addAll(getReceivedDateErrors(request));
        errors.addAll(getOpenDuringRangeErrors(request));
        errors.addAll(getPageSizeErrors(request));
        errors.addAll(getPageNumberErrors(request));
        errors.addAll(getOrderByErrors(request));
        if (errors != null && errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    private Set<String> getInformedOncErrors(ComplaintSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!CollectionUtils.isEmpty(request.getInformedOncStrings())) {
            request.getInformedOncStrings().stream()
                .map(booleanStr -> StringUtils.normalizeSpace(booleanStr))
                .filter(booleanStr -> !StringUtils.isEmpty(booleanStr) && !isParseableBoolean(booleanStr))
                .forEach(badBooleanStr -> addInvalidBooleanError(errors, badBooleanStr, "Informed ONC"));
        }
        return errors;
    }

    private Set<String> getAtlContactedErrors(ComplaintSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!CollectionUtils.isEmpty(request.getOncAtlContactedStrings())) {
            request.getOncAtlContactedStrings().stream()
                .map(booleanStr -> StringUtils.normalizeSpace(booleanStr))
                .filter(booleanStr -> !StringUtils.isEmpty(booleanStr) && !isParseableBoolean(booleanStr))
                .forEach(badBooleanStr -> addInvalidBooleanError(errors, badBooleanStr, "ONC-ATL Contacted"));
        }
        return errors;
    }

    private Set<String> getComplainantContactedErrors(ComplaintSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!CollectionUtils.isEmpty(request.getComplainantContactedStrings())) {
            request.getComplainantContactedStrings().stream()
                .map(booleanStr -> StringUtils.normalizeSpace(booleanStr))
                .filter(booleanStr -> !StringUtils.isEmpty(booleanStr) && !isParseableBoolean(booleanStr))
                .forEach(badBooleanStr -> addInvalidBooleanError(errors, badBooleanStr, "Complainant Contacted"));
        }
        return errors;
    }

    private Set<String> getDeveloperContactedErrors(ComplaintSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!CollectionUtils.isEmpty(request.getDeveloperContactedStrings())) {
            request.getDeveloperContactedStrings().stream()
                .map(booleanStr -> StringUtils.normalizeSpace(booleanStr))
                .filter(booleanStr -> !StringUtils.isEmpty(booleanStr) && !isParseableBoolean(booleanStr))
                .forEach(badBooleanStr -> addInvalidBooleanError(errors, badBooleanStr, "Developer Contacted"));
        }
        return errors;
    }

    private void addInvalidBooleanError(Set<String> errors, String badBooleanVal, String type) {
            errors.add(msgUtil.getMessage("search.complaint.invalidBoolean", badBooleanVal, type));
    }

    private Set<String> getAcbIdsErrors(ComplaintSearchRequest request) {
        Set<String> errors = new HashSet<String>();
        if (!CollectionUtils.isEmpty(request.getAcbIds())) {
            request.getAcbIds().stream()
                .forEach(acbId -> errors.addAll(getAcbIdErrors(acbId)));
        }
        return errors;
    }

    private Set<String> getAcbIdErrors(Long acbId) {
        Set<String> errors = new HashSet<String>();
        try {
            acbDao.getById(acbId);
        } catch (EntityRetrievalException ex) {
            errors.add(msgUtil.getMessage("search.complaint.acbId.doesNotExist", acbId.toString()));
        }
        return errors;
    }

    private Set<String> getComplainantTypeNameErrors(ComplaintSearchRequest request) {
        if (CollectionUtils.isEmpty(request.getComplainantTypeNames())) {
            return Collections.emptySet();
        }

        List<ComplainantType> allComplainantTypes = complaintDao.getComplainantTypes();
        Set<String> allComplainantTypeNames;
        if (!CollectionUtils.isEmpty(allComplainantTypes)) {
            allComplainantTypeNames = allComplainantTypes.stream().map(kvm -> kvm.getName()).collect(Collectors.toSet());
        } else {
            allComplainantTypeNames = Collections.emptySet();
        }
        return request.getComplainantTypeNames().stream()
            .filter(name -> !isInSet(name, allComplainantTypeNames))
            .map(name -> msgUtil.getMessage("search.complaint.complainantType.invalid", name))
            .collect(Collectors.toSet());
    }

    private Set<String> getCurrentStatusErrors(ComplaintSearchRequest request) {
        if (CollectionUtils.isEmpty(request.getCurrentStatusNames())) {
            return Collections.emptySet();
        }
        return request.getCurrentStatusNames().stream()
                .filter(name -> !isInSet(name, complaintStatuses))
                .map(name -> msgUtil.getMessage("search.complaint.complaintStatus.invalid", name,
                        complaintStatuses.stream().collect(Collectors.joining(", "))))
                .collect(Collectors.toSet());
    }

    private Set<String> getListingIdFormatErrors(Set<String> listingIdStrings) {
        if (listingIdStrings != null && listingIdStrings.size() > 0) {
            return listingIdStrings.stream()
                .filter(listingId -> !StringUtils.isBlank(listingId))
                .filter(listingId -> !isParseableLong(listingId.trim()))
                .map(listingId -> msgUtil.getMessage("search.complaint.listingId.invalid", listingId))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getSurveillanceIdFormatErrors(Set<String> survIdStrings) {
        if (survIdStrings != null && survIdStrings.size() > 0) {
            return survIdStrings.stream()
                .filter(survId -> !StringUtils.isBlank(survId))
                .filter(survId -> !isParseableLong(survId.trim()))
                .map(survId -> msgUtil.getMessage("search.complaint.surveillanceId.invalid", survId))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getCriteriaIdFormatErrors(Set<String> criteriaIdStrings) {
        if (criteriaIdStrings != null && criteriaIdStrings.size() > 0) {
            return criteriaIdStrings.stream()
                .filter(criteriaId -> !StringUtils.isBlank(criteriaId))
                .filter(criteriaId -> !isParseableLong(criteriaId.trim()))
                .map(criteriaId -> msgUtil.getMessage("search.complaint.certificationCriteriaId.invalid", criteriaId))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isParseableLong(String value) {
        try {
            Long.parseLong(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private Set<String> getClosedDateErrors(ComplaintSearchRequest request) {
        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isBlank(request.getClosedDateStart())) {
            try {
                LocalDate.parse(request.getClosedDateStart(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.closedDate.invalid",
                        request.getClosedDateStart(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isBlank(request.getClosedDateEnd())) {
            try {
                LocalDate.parse(request.getClosedDateEnd(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.closedDate.invalid",
                        request.getClosedDateEnd(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        return errors;
    }

    private Set<String> getReceivedDateErrors(ComplaintSearchRequest request) {
        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isBlank(request.getReceivedDateStart())) {
            try {
                LocalDate.parse(request.getReceivedDateStart(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.receivedDate.invalid",
                        request.getReceivedDateStart(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isBlank(request.getReceivedDateEnd())) {
            try {
                LocalDate.parse(request.getReceivedDateEnd(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.receivedDate.invalid",
                        request.getReceivedDateEnd(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        return errors;
    }

    private Set<String> getOpenDuringRangeErrors(ComplaintSearchRequest request) {
        Set<String> errors = new LinkedHashSet<String>();
        if (!StringUtils.isBlank(request.getOpenDuringRangeStart())) {
            try {
                LocalDate.parse(request.getOpenDuringRangeStart(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.openDuring.invalid",
                        request.getOpenDuringRangeStart(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (!StringUtils.isBlank(request.getOpenDuringRangeEnd())) {
            try {
                LocalDate.parse(request.getOpenDuringRangeEnd(), dateFormatter);
            } catch (DateTimeParseException ex) {
                errors.add(msgUtil.getMessage("search.complaint.openDuring.invalid",
                        request.getOpenDuringRangeEnd(),
                        ComplaintSearchRequest.DATE_SEARCH_FORMAT));
            }
        }

        if (StringUtils.isBlank(request.getOpenDuringRangeStart()) && !StringUtils.isBlank(request.getOpenDuringRangeEnd())
            || !StringUtils.isBlank(request.getOpenDuringRangeStart()) && StringUtils.isBlank(request.getOpenDuringRangeEnd())) {
            errors.add(msgUtil.getMessage("search.complaint.openDuring.missingStartOrEnd"));
        }
        return errors;
    }

    private Set<String> getPageNumberErrors(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageNumberString())
                && !isParseableInteger(request.getPageNumberString())) {
            return Stream.of(
                    msgUtil.getMessage("search.complaint.pageNumber.invalid", request.getPageNumberString()))
                    .collect(Collectors.toSet());
        }
        Integer pageNumber = request.getPageNumber();
        if (pageNumber != null && pageNumber < 0) {
            return Stream.of(
                    msgUtil.getMessage("search.complaint.pageNumber.outOfRange", request.getPageNumber()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getPageSizeErrors(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageSizeString())
                && !isParseableInteger(request.getPageSizeString())) {
            return Stream.of(
                    msgUtil.getMessage("search.complaint.pageSize.invalid", request.getPageSizeString()))
                    .collect(Collectors.toSet());
        }
        Integer pageSize = request.getPageSize();
        if (pageSize != null
                && (pageSize > ComplaintSearchRequest.MAX_PAGE_SIZE || pageSize < ComplaintSearchRequest.MIN_PAGE_SIZE)) {
            return Stream.of(
                    msgUtil.getMessage("search.complaint.pageSize.outOfRange",
                            ComplaintSearchRequest.MIN_PAGE_SIZE,
                            ComplaintSearchRequest.MAX_PAGE_SIZE,
                            pageSize.toString()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<String> getOrderByErrors(ComplaintSearchRequest request) {
        if (request.getOrderBy() == null
                && !StringUtils.isBlank(request.getOrderByString())) {
            return Stream.of(msgUtil.getMessage("search.complaint.orderBy.invalid",
                    request.getOrderByString(),
                    Stream.of(OrderByOption.values())
                        .map(value -> value.name())
                        .collect(Collectors.joining(", "))))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isParseableBoolean(String value) {
        Boolean booleanObj = null;
        try {
            booleanObj = BooleanUtils.toBooleanObject(value);
        } catch (Exception ex) {
            return false;
        }
        return booleanObj != null;
    }

    private boolean isParseableInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private boolean isInSet(String value, Set<String> setToSearch) {
        if (setToSearch == null) {
            return false;
        }
        return setToSearch.stream()
            .filter(item -> item.equalsIgnoreCase(value))
            .count() > 0;
    }
}
