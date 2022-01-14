package gov.healthit.chpl.changerequest.domain;

import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.domain.AttestationAnswer;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationQuestion;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationResponseEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDetailsEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestWebsiteEntity;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;

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

    public static ChangeRequest convert(ChangeRequestEntity entity) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(entity.getId());
        cr.setChangeRequestType(convert(entity.getChangeRequestType()));
        cr.setDeveloper(new Developer(new DeveloperDTO(entity.getDeveloper())));
        cr.setSubmittedDate(entity.getCreationDate());
        return cr;
    }

    public static ChangeRequestStatus convert(ChangeRequestStatusEntity entity) {
        ChangeRequestStatus status = new ChangeRequestStatus();
        status.setId(entity.getId());
        status.setChangeRequestStatusType(convert(entity.getChangeRequestStatusType()));
        status.setComment(entity.getComment());
        status.setStatusChangeDate(entity.getStatusChangeDate());
        if (entity.getCertificationBody() != null) {
            status.setCertificationBody(new CertificationBody(new CertificationBodyDTO(entity.getCertificationBody())));
        }
        status.setUserPermission(entity.getUserPermission().toDomain());
        return status;
    }

    public static ChangeRequestWebsite convert(ChangeRequestWebsiteEntity entity) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        crWebsite.setId(entity.getId());
        crWebsite.setWebsite(entity.getWebsite());
        return crWebsite;
    }

    public static ChangeRequestDeveloperDetails convert(ChangeRequestDeveloperDetailsEntity entity) {
        ChangeRequestDeveloperDetails crDev = new ChangeRequestDeveloperDetails();
        crDev.setId(entity.getId());
        crDev.setSelfDeveloper(entity.getSelfDeveloper());
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

    public static ChangeRequestAttestation convert(ChangeRequestAttestationEntity entity) {
        return ChangeRequestAttestation.builder()
                .attestationPeriod(AttestationPeriod.builder()
                        .id(entity.getPeriod().getId())
                        .periodStart(entity.getPeriod().getPeriodStart())
                        .periodEnd(entity.getPeriod().getPeriodEnd())
                        .description(entity.getPeriod().getDescription())
                        .build())
                .responses(entity.getResponses().stream()
                        .map(resp -> convert(resp))
                        .collect(Collectors.toList()))
                .build();
    }

    private static AttestationResponse convert(ChangeRequestAttestationResponseEntity entity) {
        return AttestationResponse.builder()
                .id(entity.getId())
                .question(AttestationQuestion.builder()
                        .id(entity.getQuestion().getId())
                        .question(entity.getQuestion().getQuestion())
                        .build())
                .answer(AttestationAnswer.builder()
                        .id(entity.getQuestion().getId())
                        .answer(entity.getAnswer().getAnswer())
                        .build())
                .build();
    }
}
