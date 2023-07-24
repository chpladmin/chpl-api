package gov.healthit.chpl.changerequest.domain;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDemographicsEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult.CurrentStatusSearchResult;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.DateUtil;

public final class ChangeRequestConverter {

    private ChangeRequestConverter() {
        // not called
    }

    public static ChangeRequestStatusType convert(ChangeRequestStatusTypeEntity entity) {
        ChangeRequestStatusType status = new ChangeRequestStatusType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public static ChangeRequestType convert(ChangeRequestTypeEntity entity) {
        ChangeRequestType status = new ChangeRequestType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public static ChangeRequestSearchResult toSearchResult(ChangeRequestAttestationSubmissionEntity entity) {
        ChangeRequestEntity cr = entity.getChangeRequest();
        return toSearchResult(cr);
    }

    public static ChangeRequestSearchResult toSearchResult(ChangeRequestEntity entity) {
        return ChangeRequestSearchResult.builder()
        .id(entity.getId())
        .changeRequestType(IdNamePair.builder()
                .id(entity.getChangeRequestType().getId())
                .name(entity.getChangeRequestType().getName())
                .build())
        .developer(IdNamePair.builder()
                .id(entity.getDeveloper().getId())
                .name(entity.getDeveloper().getName())
                .build())
        .submittedDateTime(DateUtil.toLocalDateTime(entity.getCreationDate().getTime()))
        .currentStatus(convertSearchResult(getLatestStatus(entity.getStatuses())))
        .certificationBodies(entity.getCertificationBodies().stream()
                .map(e -> IdNamePair.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList())
        .build();
    }

    public static CurrentStatusSearchResult convertSearchResult(ChangeRequestStatusEntity entity) {
        return CurrentStatusSearchResult.builder()
                .id(entity.getChangeRequestStatusType().getId())
                .name(entity.getChangeRequestStatusType().getName())
                .statusChangeDateTime(DateUtil.toLocalDateTime(entity.getStatusChangeDate().getTime()))
                .build();
    }

    public static ChangeRequest convert(ChangeRequestEntity entity) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(entity.getId());
        cr.setChangeRequestType(convert(entity.getChangeRequestType()));
        cr.setDeveloper(entity.getDeveloper().toDomain());
        cr.setSubmittedDateTime(DateUtil.toLocalDateTime(entity.getCreationDate().getTime()));
        entity.getStatuses().stream()
            .map(statusEntity -> convert(statusEntity))
            .forEach(status -> cr.getStatuses().add(status));
        cr.setCurrentStatus(getLatestStatus(cr.getStatuses()));
        cr.setCertificationBodies(entity.getCertificationBodies().stream()
                .map(e -> e.toDomain())
                .toList());
        return cr;
    }

    private static ChangeRequestStatus getLatestStatus(List<ChangeRequestStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return null;
        }
        ChangeRequestStatus newest = statuses.get(0);
        for (ChangeRequestStatus event : statuses) {
            if (event.getStatusChangeDateTime().isAfter(newest.getStatusChangeDateTime())) {
                newest = event;
            }
        }
        return newest;
    }

    private static ChangeRequestStatusEntity getLatestStatus(Set<ChangeRequestStatusEntity> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return null;
        }
        ChangeRequestStatusEntity newest = null;
        for (ChangeRequestStatusEntity event : statuses) {
            if (newest == null) {
                newest = event;
            } else if (event.getStatusChangeDate().after(newest.getStatusChangeDate())) {
                newest = event;
            }
        }
        return newest;
    }

    public static ChangeRequestStatus convert(ChangeRequestStatusEntity entity) {
        ChangeRequestStatus status = new ChangeRequestStatus();
        status.setId(entity.getId());
        status.setChangeRequestStatusType(convert(entity.getChangeRequestStatusType()));
        status.setComment(entity.getComment());
        status.setStatusChangeDateTime(DateUtil.toLocalDateTime(entity.getStatusChangeDate().getTime()));
        if (entity.getCertificationBody() != null) {
            status.setCertificationBody(entity.getCertificationBody().toDomain());
        }
        status.setUserPermission(entity.getUserPermission().toDomain());
        return status;
    }

    public static ChangeRequestDeveloperDemographics convert(ChangeRequestDeveloperDemographicsEntity entity) {
        ChangeRequestDeveloperDemographics crDev = new ChangeRequestDeveloperDemographics();
        crDev.setId(entity.getId());
        crDev.setSelfDeveloper(entity.getSelfDeveloper());
        crDev.setWebsite(entity.getWebsite());
        Address address = null;
        if (entity.getStreetLine1() != null || entity.getStreetLine2() != null
                || entity.getCity() != null || entity.getState() != null
                || entity.getZipcode() != null || entity.getCountry() != null) {
            address = new Address();
            address.setLine1(entity.getStreetLine1());
            address.setLine2(entity.getStreetLine2());
            address.setCity(entity.getCity());
            address.setState(entity.getState());
            address.setZipcode(entity.getZipcode());
            address.setCountry(entity.getCountry());
        }
        crDev.setAddress(address);
        PointOfContact contact = null;
        if (entity.getContactFullName() != null || entity.getContactEmail() != null
                || entity.getContactPhoneNumber() != null || entity.getContactTitle() != null) {
            contact = new PointOfContact();
            contact.setFullName(entity.getContactFullName());
            contact.setEmail(entity.getContactEmail());
            contact.setPhoneNumber(entity.getContactPhoneNumber());
            contact.setTitle(entity.getContactTitle());
        }
        crDev.setContact(contact);
        return crDev;
    }
}
