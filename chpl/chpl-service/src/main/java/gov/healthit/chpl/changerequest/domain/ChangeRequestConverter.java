package gov.healthit.chpl.changerequest.domain;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDemographicsEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestListingUrlEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult.CurrentStatusSearchResult;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public final class ChangeRequestConverter {

    private ChplUserToCognitoUserUtil chplUserToCognitoUserUtil;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    public ChangeRequestConverter(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil, @Lazy CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.chplUserToCognitoUserUtil = chplUserToCognitoUserUtil;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
    }

    public ChangeRequestStatusType convert(ChangeRequestStatusTypeEntity entity) {
        ChangeRequestStatusType status = new ChangeRequestStatusType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public ChangeRequestType convert(ChangeRequestTypeEntity entity) {
        ChangeRequestType status = new ChangeRequestType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public ChangeRequestSearchResult toSearchResult(ChangeRequestAttestationSubmissionEntity entity) {
        ChangeRequestEntity cr = entity.getChangeRequest();
        return toSearchResult(cr);
    }

    public ChangeRequestSearchResult toSearchResult(ChangeRequestEntity entity) {
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

    public CurrentStatusSearchResult convertSearchResult(ChangeRequestStatusEntity entity) {
        return CurrentStatusSearchResult.builder()
                .id(entity.getChangeRequestStatusType().getId())
                .name(entity.getChangeRequestStatusType().getName())
                .statusChangeDateTime(DateUtil.toLocalDateTime(entity.getStatusChangeDate().getTime()))
                .build();
    }

    public ChangeRequest convert(ChangeRequestEntity entity) {
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

    private ChangeRequestStatus getLatestStatus(List<ChangeRequestStatus> statuses) {
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

    private ChangeRequestStatusEntity getLatestStatus(Set<ChangeRequestStatusEntity> statuses) {
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

    public ChangeRequestStatus convert(ChangeRequestStatusEntity entity) {
        User lastModifiedUser = chplUserToCognitoUserUtil.getUser(entity.getLastModifiedUser(), entity.getLastModifiedSsoUser());
        return ChangeRequestStatus.builder()
                .id(entity.getId())
                .changeRequestStatusType(convert(entity.getChangeRequestStatusType()))
                .comment(entity.getComment())
                .statusChangeDateTime(DateUtil.toLocalDateTime(entity.getStatusChangeDate().getTime()))
                .certificationBody(entity.getCertificationBody() != null ? entity.getCertificationBody().toDomain() : null)
                .userGroupName(entity.getUserGroupName())
                .actingUser(lastModifiedUser != null ? lastModifiedUser.getEmail() : null)
                .build();
    }

    public ChangeRequestDeveloperDemographics convert(ChangeRequestDeveloperDemographicsEntity entity) {
        if (entity == null) {
            return null;
        }

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
                || entity.getContactPhoneNumber() != null) {
            contact = new PointOfContact();
            contact.setFullName(entity.getContactFullName());
            contact.setEmail(entity.getContactEmail());
            contact.setPhoneNumber(entity.getContactPhoneNumber());
        }
        crDev.setContact(contact);
        return crDev;
    }

    public ChangeRequestListingUrl convert(ChangeRequestListingUrlEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            ChangeRequestListingUrl crListingUrl = new ChangeRequestListingUrl();
            crListingUrl.setId(entity.getId());
            crListingUrl.setUrl(entity.getUrl());
            crListingUrl.setListing(certifiedProductDetailsManager.getCertifiedProductDetails(entity.getListingId()));
            return crListingUrl;
        } catch (Exception e) {
            LOGGER.error("Error getting listing {} for change request listing URL.", entity.getListingId(), e);
            return null;
        }
    }
}
