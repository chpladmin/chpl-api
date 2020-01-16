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

    /**
     * The internal ID of the developer.
     */
    @XmlElement(required = true)
    private Long developerId;

    /**
     * A four-digit code assigned to each developer when it was created.
     */
    @XmlElement(required = true)
    private String developerCode;

    /**
     * The name of the developer or vendor of the certified health IT product
     * being uploaded. It is applicable to 2014 and 2015 Edition. If uploading a
     * certified product from a developer that already exists in the CHPL
     * database, please use the CHPL Developer management functionality to
     * ensure that the name of the developer matches the database record to
     * prevent duplication.
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Website of health IT developer. Fully qualified URL which is reachable
     * via web browser validation and verification. This variable is applicable
     * for 2014 and 2015 Edition.
     */
    @XmlElement(required = false, nillable = true)
    private String website;

    /**
     * Indication of whether a health IT developer is a "self-developer" or not.
     */
    @XmlElement(required = true)
    private Boolean selfDeveloper;

    /**
     * Developer's physical address
     */
    @XmlElement(required = false, nillable = true)
    private Address address;

    /**
     * Contact information for the developer.
     */
    @XmlElement(required = false, nillable = true)
    private Contact contact;

    @XmlTransient
    private String lastModifiedDate;

    @XmlTransient
    private Boolean deleted;

    /**
     * Transparency attestations between each certification body and the
     * developer.
     */
    @XmlElement(required = false, nillable = true)
    private List<TransparencyAttestationMap> transparencyAttestations;

    /**
     * Status changes that have occurred on the developer.
     */
    @XmlElementWrapper(name = "statusEvents", nillable = true, required = false)
    @XmlElement(name = "statusEvent",required = false, nillable = true)
    private List<DeveloperStatusEvent> statusEvents;

    /**
     * The status of a developer with certified Health IT. Allowable values are
     * "Active", "Suspended by ONC", or "Under Certification Ban by ONC"
     */
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
        this.selfDeveloper = dto.getSelfDeveloper();
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
                toAdd.setAttestation(map.getTransparencyAttestation());
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

    public Boolean getSelfDeveloper() {
        return selfDeveloper;
    }

    public void setSelfDeveloper(Boolean selfDeveloper) {
        this.selfDeveloper = selfDeveloper;
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
