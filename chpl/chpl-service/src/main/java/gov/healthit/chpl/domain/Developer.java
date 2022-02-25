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

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
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
     * The name of the developer or vendor of the certified health IT product being uploaded. It is applicable to 2014
     * and 2015 Edition. If uploading a certified product from a developer that already exists in the CHPL database,
     * please use the CHPL Developer management functionality to ensure that the name of the developer matches the
     * database record to prevent duplication.
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Website of health IT developer. Fully qualified URL which is reachable via web browser validation and
     * verification. This variable is applicable for 2014 and 2015 Edition.
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
    private PointOfContact contact;

    @XmlTransient
    private String lastModifiedDate;

    @XmlTransient
    private Boolean deleted;

    /**
     * Status changes that have occurred on the developer.
     */
    @XmlElementWrapper(name = "statusEvents", nillable = true, required = false)
    @XmlElement(name = "statusEvent", required = false, nillable = true)
    @Singular
    private List<DeveloperStatusEvent> statusEvents;

    @XmlTransient
    @JsonIgnore
    private String userEnteredName;

    @XmlTransient
    @JsonIgnore
    private String userEnteredWebsite;

    @XmlTransient
    @JsonIgnore
    private String userEnteredSelfDeveloper;

    @XmlTransient
    @JsonIgnore
    private Address userEnteredAddress;

    @XmlTransient
    @JsonIgnore
    private PointOfContact userEnteredPointOfContact;

    public Developer() {
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
            this.contact = new PointOfContact(dto.getContact());
        }

        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }

        if (dto.getStatusEvents() != null && dto.getStatusEvents().size() > 0) {
            for (DeveloperStatusEventDTO historyItem : dto.getStatusEvents()) {
                DeveloperStatusEvent toAdd = new DeveloperStatusEvent(historyItem);
                this.statusEvents.add(toAdd);
            }
        }
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
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

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(String developerCode) {
        this.developerCode = developerCode;
    }

    public PointOfContact getContact() {
        return contact;
    }

    public void setContact(PointOfContact contact) {
        this.contact = contact;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The status of a developer with certified Health IT. Allowable values are "Active", "Suspended by ONC", or "Under
     * Certification Ban by ONC"
     */
    @XmlElement(required = false, nillable = true)
    public DeveloperStatus getStatus() {
        if (CollectionUtils.isEmpty(this.getStatusEvents())) {
            return null;
        }

        DeveloperStatusEvent newest = this.getStatusEvents().get(0);
        for (DeveloperStatusEvent event : this.getStatusEvents()) {
            if (event.getStatusDate().after(newest.getStatusDate())) {
                newest = event;
            }
        }
        return newest.getStatus();
    }

    public List<DeveloperStatusEvent> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(List<DeveloperStatusEvent> statusEvents) {
        this.statusEvents = statusEvents;
    }

    public String getUserEnteredName() {
        return userEnteredName;
    }

    public void setUserEnteredName(String userEnteredName) {
        this.userEnteredName = userEnteredName;
    }

    public String getUserEnteredWebsite() {
        return userEnteredWebsite;
    }

    public void setUserEnteredWebsite(String userEnteredWebsite) {
        this.userEnteredWebsite = userEnteredWebsite;
    }

    public Address getUserEnteredAddress() {
        return userEnteredAddress;
    }

    public void setUserEnteredAddress(Address userEnteredAddress) {
        this.userEnteredAddress = userEnteredAddress;
    }

    public PointOfContact getUserEnteredPointOfContact() {
        return userEnteredPointOfContact;
    }

    public void setUserEnteredPointOfContact(PointOfContact userEnteredPointOfContact) {
        this.userEnteredPointOfContact = userEnteredPointOfContact;
    }

    public String getUserEnteredSelfDeveloper() {
        return userEnteredSelfDeveloper;
    }

    public void setUserEnteredSelfDeveloper(String userEnteredSelfDeveloper) {
        this.userEnteredSelfDeveloper = userEnteredSelfDeveloper;
    }
}
