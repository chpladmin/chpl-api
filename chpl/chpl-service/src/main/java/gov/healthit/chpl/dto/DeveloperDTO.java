package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import gov.healthit.chpl.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeveloperDTO implements Serializable {
    private static final long serialVersionUID = -2492373079266782228L;
    private String developerCode;
    private Long id;
    private AddressDTO address;
    private ContactDTO contact;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;
    private String website;
    private List<DeveloperStatusEventDTO> statusEvents;
    private List<DeveloperACBMapDTO> transparencyAttestationMappings;
    private Statuses statuses;

    public DeveloperDTO() {
        this.transparencyAttestationMappings = new ArrayList<DeveloperACBMapDTO>();
        this.statusEvents = new ArrayList<DeveloperStatusEventDTO>();
    }

    public DeveloperDTO(DeveloperEntitySimple entity) {
        this();
        this.id = entity.getId();
        this.developerCode = entity.getDeveloperCode();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.name = entity.getName();
        this.website = entity.getWebsite();
    }

    public DeveloperDTO(DeveloperEntity entity) {
        this();
        this.id = entity.getId();
        this.developerCode = entity.getDeveloperCode();
        if (entity.getAddress() != null) {
            this.address = new AddressDTO(entity.getAddress());
        }
        if (entity.getContact() != null) {
            this.contact = new ContactDTO(entity.getContact());
        }
        if (entity.getStatusEvents() != null && entity.getStatusEvents().size() > 0) {
            for (DeveloperStatusEventEntity statusEntity : entity.getStatusEvents()) {
                this.statusEvents.add(new DeveloperStatusEventDTO(statusEntity));
            }
        }

        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.name = entity.getName();
        this.website = entity.getWebsite();
        if (entity.getDeveloperCertificationStatusesEntity() != null) {
            this.statuses = new Statuses(entity.getDeveloperCertificationStatusesEntity().getActive(),
                    entity.getDeveloperCertificationStatusesEntity().getRetired(),
                    entity.getDeveloperCertificationStatusesEntity().getWithdrawnByDeveloper(),
                    entity.getDeveloperCertificationStatusesEntity().getWithdrawnByAcb(),
                    entity.getDeveloperCertificationStatusesEntity().getSuspendedByAcb(),
                    entity.getDeveloperCertificationStatusesEntity().getSuspendedByOnc(),
                    entity.getDeveloperCertificationStatusesEntity().getTerminatedByOnc());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public ContactDTO getContact() {
        return contact;
    }

    public void setContact(final ContactDTO contact) {
        this.contact = contact;
    }

    public List<DeveloperACBMapDTO> getTransparencyAttestationMappings() {
        return transparencyAttestationMappings;
    }

    public void setTransparencyAttestationMappings(final List<DeveloperACBMapDTO> transparencyAttestationMappings) {
        this.transparencyAttestationMappings = transparencyAttestationMappings;
    }

    public Statuses getStatuses() {
        return statuses;
    }

    public void setStatuses(final Statuses statuses) {
        this.statuses = statuses;
    }

    public List<DeveloperStatusEventDTO> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(final List<DeveloperStatusEventDTO> statusEvents) {
        this.statusEvents = statusEvents;
    }

    public DeveloperStatusEventDTO getStatus() {
        DeveloperStatusEventDTO mostRecentStatus = null;

        if (getStatusEvents() != null && getStatusEvents().size() > 0) {
            for (DeveloperStatusEventDTO currStatusHistory : getStatusEvents()) {
                if (mostRecentStatus == null) {
                    mostRecentStatus = currStatusHistory;
                } else {
                    if (currStatusHistory.getStatusDate().getTime() > mostRecentStatus.getStatusDate().getTime()) {
                        mostRecentStatus = currStatusHistory;
                    }
                }
            }
        }
        return mostRecentStatus;
    }
}
