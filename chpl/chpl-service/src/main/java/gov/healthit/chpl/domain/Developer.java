package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.contact.PointOfContact;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class Developer implements Serializable {
    private static final long serialVersionUID = 7341544844577617247L;

    /**
     * This property exists solely to be able to deserialize developer activity events.
     * When deserializing the activity we sometimes care about the developer ID.
     * This property should not be visible in the generated XSD (and eventually gone from the JSON).
     */
    @Deprecated
    @XmlTransient
    @DeprecatedResponseField(removalDate = "2022-12-15",
        message = "This field is deprecated and will be removed from the response data in a future release. Please replace usage of the 'developerId' field with 'id'.")
    private Long developerId;

    /**
     * The internal ID of the developer.
     */
    @XmlElement(required = true)
    private Long id;

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
    @Builder.Default
    private List<DeveloperStatusEvent> statusEvents = new ArrayList<DeveloperStatusEvent>();

    /**
     * Public attestations submitted by the developer.
     */
    @XmlElementWrapper(name = "attestations", nillable = true, required = false)
    @XmlElement(name = "attestation")
    private List<PublicAttestation> attestations;

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

    @Deprecated
    public Long getDeveloperId() {
        return developerId;
    }

    @Deprecated
    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

        return getMostRecentStatusEvent().getStatus();
    }

    @JsonIgnore
    @XmlTransient
    public DeveloperStatusEvent getMostRecentStatusEvent() {
        if (CollectionUtils.isEmpty(this.getStatusEvents())) {
            return null;
        }

        DeveloperStatusEvent newest = this.getStatusEvents().get(0);
        for (DeveloperStatusEvent event : this.getStatusEvents()) {
            if (event.getStatusDate().after(newest.getStatusDate())) {
                newest = event;
            }
        }
        return newest;
    }

    public List<DeveloperStatusEvent> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(List<DeveloperStatusEvent> statusEvents) {
        this.statusEvents = statusEvents;
    }

    public List<PublicAttestation> getAttestations() {
        return attestations;
    }

    public void setAttestations(List<PublicAttestation> attestations) {
        this.attestations = attestations;
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

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((developerCode == null) ? 0 : developerCode.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((selfDeveloper == null) ? 0 : selfDeveloper.hashCode());
        result = prime * result + ((statusEvents == null) ? 0 : statusEvents.hashCode());
        result = prime * result + ((website == null) ? 0 : website.hashCode());
        return result;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Developer other = (Developer) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (contact == null) {
            if (other.contact != null) {
                return false;
            }
        } else if (!contact.equals(other.contact)) {
            return false;
        }
        if (developerCode == null) {
            if (other.developerCode != null) {
                return false;
            }
        } else if (!developerCode.equals(other.developerCode)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (selfDeveloper == null) {
            if (other.selfDeveloper != null) {
                return false;
            }
        } else if (!selfDeveloper.equals(other.selfDeveloper)) {
            return false;
        }
        if (statusEvents == null) {
            if (other.statusEvents != null) {
                return false;
            }
        } else if (!isStatusEventListEqual(other.statusEvents)) {
            return false;
        }
        if (website == null) {
            if (other.website != null) {
                return false;
            }
        } else if (!website.equals(other.website)) {
            return false;
        }
        return true;
    }

    private boolean isStatusEventListEqual(List<DeveloperStatusEvent> other) {
        if (statusEvents.size() != other.size()) {
            return false;
        } else {
            // Make copies of both lists and order them
            List<DeveloperStatusEvent> clonedThis = statusEvents.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEvent::getStatusDate))
                    .toList();
            List<DeveloperStatusEvent> clonedOther = other.stream()
                    .sorted(Comparator.comparing(DeveloperStatusEvent::getStatusDate))
                    .toList();
            return clonedThis.equals(clonedOther);
        }
    }
}
