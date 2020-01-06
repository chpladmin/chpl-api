package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Developer implements Serializable {
    private static final long serialVersionUID = 7341544844577617247L;

    @XmlElement(required = true)
    private Long developerId;

    @XmlElement(required = true)
    private String developerCode;

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = false, nillable = true)
    private String website;

    @XmlElement(required = false, nillable = true)
    private Address address;

    @XmlElement(required = false, nillable = true)
    private Contact contact;

    @XmlTransient
    private String lastModifiedDate;

    @XmlTransient
    private Boolean deleted;

    @XmlElement(required = false, nillable = true)
    private List<TransparencyAttestationMap> transparencyAttestations;

    @XmlElementWrapper(name = "statusEvents", nillable = true, required = false)
    @XmlElement(name = "statusEvent", required = false, nillable = true)
    private List<DeveloperStatusEvent> statusEvents;

    @XmlElement(required = false, nillable = true)
    private DeveloperStatus status;

    public Developer() {
        this.transparencyAttestations = new ArrayList<TransparencyAttestationMap>();
        this.statusEvents = new ArrayList<DeveloperStatusEvent>();
    }

    public Developer(DeveloperDTO dto) {
        this();
        this.developerId = dto.getId();
        this.developerCode = dto.getDeveloperCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        this.deleted = dto.getDeleted();
        if (dto.getAddress() != null) {
            this.address = new Address(dto.getAddress());
        }
        if (dto.getContact() != null) {
            this.contact = new Contact(dto.getContact());
        }

        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }

        if (dto.getTransparencyAttestationMappings() != null && dto.getTransparencyAttestationMappings().size() > 0) {
            for (DeveloperACBMapDTO map : dto.getTransparencyAttestationMappings()) {
                TransparencyAttestationMap toAdd = new TransparencyAttestationMap();
                toAdd.setAcbId(map.getAcbId());
                toAdd.setAcbName(map.getAcbName());
                if (map.getTransparencyAttestation() != null) {
                    toAdd.setAttestation(new TransparencyAttestation(map.getTransparencyAttestation()));
                }
                this.transparencyAttestations.add(toAdd);
            }
        }

        if (dto.getStatusEvents() != null && dto.getStatusEvents().size() > 0) {
            for (DeveloperStatusEventDTO historyItem : dto.getStatusEvents()) {
                DeveloperStatusEvent toAdd = new DeveloperStatusEvent(historyItem);
                this.statusEvents.add(toAdd);
            }

            this.status = new DeveloperStatus(dto.getStatus().getStatus());
        }
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }

    public List<TransparencyAttestationMap> getTransparencyAttestations() {
        return transparencyAttestations;
    }

    public void setTransparencyAttestations(final List<TransparencyAttestationMap> transparencyAttestations) {
        this.transparencyAttestations = transparencyAttestations;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public DeveloperStatus getStatus() {
        return status;
    }

    public void setStatus(final DeveloperStatus status) {
        this.status = status;
    }

    public List<DeveloperStatusEvent> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(final List<DeveloperStatusEvent> statusEvents) {
        this.statusEvents = statusEvents;
    }
}
