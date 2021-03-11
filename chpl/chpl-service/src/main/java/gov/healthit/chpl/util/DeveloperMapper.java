package gov.healthit.chpl.util;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.domain.TransparencyAttestationMap;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeveloperMapper {

    public DeveloperDTO from(DeveloperEntitySimple entity) {
        DeveloperDTO dto = null;
        if (entity != null) {
            dto = new DeveloperDTO();
            dto.setId(entity.getId());
            dto.setDeveloperCode(entity.getDeveloperCode());
            dto.setCreationDate(entity.getCreationDate());
            dto.setDeleted(entity.isDeleted());
            dto.setLastModifiedDate(entity.getLastModifiedDate());
            dto.setLastModifiedUser(entity.getLastModifiedUser());
            dto.setName(entity.getName());
            dto.setWebsite(entity.getWebsite());
            dto.setSelfDeveloper(entity.getSelfDeveloper());
        }
        return dto;
    }

    public DeveloperDTO from(DeveloperEntity entity) {
        DeveloperDTO dto = null;
        if (entity != null) {
            dto = new DeveloperDTO();
            dto.setId(entity.getId());
            dto.setDeveloperCode(entity.getDeveloperCode());
            if (entity.getAddress() != null) {
                dto.setAddress(new AddressDTO(entity.getAddress()));
            }
            if (entity.getContact() != null) {
                dto.setContact(new ContactDTO(entity.getContact()));
            }
            if (entity.getStatusEvents() != null && entity.getStatusEvents().size() > 0) {
                for (DeveloperStatusEventEntity statusEntity : entity.getStatusEvents()) {
                    dto.getStatusEvents().add(new DeveloperStatusEventDTO(statusEntity));
                }
            }

            dto.setCreationDate(entity.getCreationDate());
            dto.setDeleted(entity.isDeleted());
            dto.setLastModifiedDate(entity.getLastModifiedDate());
            dto.setLastModifiedUser(entity.getLastModifiedUser());
            dto.setName(entity.getName());
            dto.setWebsite(entity.getWebsite());
            dto.setSelfDeveloper(entity.getSelfDeveloper());
            if (entity.getDeveloperCertificationStatusesEntity() != null) {
                dto.setStatuses(new Statuses(entity.getDeveloperCertificationStatusesEntity().getActive(),
                        entity.getDeveloperCertificationStatusesEntity().getRetired(),
                        entity.getDeveloperCertificationStatusesEntity().getWithdrawnByDeveloper(),
                        entity.getDeveloperCertificationStatusesEntity().getWithdrawnByAcb(),
                        entity.getDeveloperCertificationStatusesEntity().getSuspendedByAcb(),
                        entity.getDeveloperCertificationStatusesEntity().getSuspendedByOnc(),
                        entity.getDeveloperCertificationStatusesEntity().getTerminatedByOnc()));
            }
        }
        return dto;
    }

    public DeveloperDTO to(Developer developer) {
        DeveloperDTO dto = null;
        if (developer != null) {
            dto = new DeveloperDTO();
            dto.setDeveloperCode(developer.getDeveloperCode());
            dto.setName(developer.getName());
            dto.setWebsite(developer.getWebsite());
            dto.setSelfDeveloper(developer.getSelfDeveloper());

            if (developer.getStatusEvents() != null && developer.getStatusEvents().size() > 0) {
                for (DeveloperStatusEvent newDeveloperStatusEvent : developer.getStatusEvents()) {
                    DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
                    DeveloperStatusDTO statusDto = new DeveloperStatusDTO();
                    statusDto.setId(newDeveloperStatusEvent.getStatus().getId());
                    statusDto.setStatusName(newDeveloperStatusEvent.getStatus().getStatus());
                    statusEvent.setStatus(statusDto);
                    statusEvent.setId(newDeveloperStatusEvent.getId());
                    statusEvent.setDeveloperId(newDeveloperStatusEvent.getDeveloperId());
                    statusEvent.setReason(newDeveloperStatusEvent.getReason());
                    statusEvent.setStatusDate(newDeveloperStatusEvent.getStatusDate());
                    dto.getStatusEvents().add(statusEvent);
                }
            }

            if (developer.getTransparencyAttestations() != null) {
                for (TransparencyAttestationMap attMap : developer.getTransparencyAttestations()) {
                    DeveloperACBMapDTO devMap = new DeveloperACBMapDTO();
                    devMap.setAcbId(attMap.getAcbId());
                    devMap.setAcbName(attMap.getAcbName());
                    if (attMap.getAttestation() != null && !StringUtils.isEmpty(attMap.getAttestation().getTransparencyAttestation())) {
                        devMap.setTransparencyAttestation(
                                new TransparencyAttestationDTO(attMap.getAttestation().getTransparencyAttestation()));
                    }
                    dto.getTransparencyAttestationMappings().add(devMap);
                }
            }

            Address developerAddress = developer.getAddress();
            if (developerAddress != null) {
                AddressDTO toCreateAddress = new AddressDTO();
                toCreateAddress.setStreetLineOne(developerAddress.getLine1());
                toCreateAddress.setStreetLineTwo(developerAddress.getLine2());
                toCreateAddress.setCity(developerAddress.getCity());
                toCreateAddress.setState(developerAddress.getState());
                toCreateAddress.setZipcode(developerAddress.getZipcode());
                toCreateAddress.setCountry(developerAddress.getCountry());
                dto.setAddress(toCreateAddress);
            }
            PointOfContact developerContact = developer.getContact();
            if (developerContact != null) {
                ContactDTO toCreateContact = new ContactDTO();
                toCreateContact.setEmail(developerContact.getEmail());
                toCreateContact.setFullName(developerContact.getFullName());
                toCreateContact.setPhoneNumber(developerContact.getPhoneNumber());
                toCreateContact.setTitle(developerContact.getTitle());
                dto.setContact(toCreateContact);
            }
            return dto;
        }
        return dto;
    }
}
