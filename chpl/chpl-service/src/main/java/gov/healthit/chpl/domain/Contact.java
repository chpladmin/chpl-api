package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ContactDTO;

/**
 * Domain object representing a Contact. Can be used as either contact information for a product / developer, or as
 * contact information for a user.
 * @author alarned
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact implements Serializable {
    private static final long serialVersionUID = 5378524206189674741L;

    /**
     * Contact internal ID
     */
    @XmlElement(required = true)
    private Long contactId;

    /**
     * Contact full name. This variable is applicable for 2014 and 2015
     * Edition, and a string variable that does not take any restrictions on
     * formatting or values.
     */
    @XmlElement(required = true)
    private String fullName;

    /**
     * Contact friendly name. This variable is applicable for 2014 and 2015 Edition,
     * and a string variable that does not take any restrictions on formatting
     * or values. Usually only used for Contacts representing Users.
     */
    @XmlElement(required = false, nillable = true)
    private String friendlyName;

    /**
     * Email address of the contact. It is applicable to 2014 and 2015 Edition.
     */
    @XmlElement(required = true)
    private String email;

    /**
     * Phone number of health IT developer contact. This variable is applicable
     * for 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values. Do not include hyphens.
     */
    @XmlElement(required = true)
    private String phoneNumber;

    /**
     * Contact title (Ms., Mr., Dr., etc)
     */
    @XmlElement(required = false, nillable = true)
    private String title;

    /**
     * Default constructor.
     */
    public Contact() {
    }

    /**
     * Constructed from a DTO.
     * @param dto the DTO
     */
    public Contact(final ContactDTO dto) {
        this.contactId = dto.getId();
        this.fullName = dto.getFullName();
        this.friendlyName = dto.getFriendlyName();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
        this.title = dto.getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contact)) {
            return false;
        }
        Contact anotherContact = (Contact) obj;
        if (this.contactId != null && anotherContact.contactId != null
                && this.contactId.longValue() == anotherContact.contactId.longValue()) {
            return true;
        } else if (this.contactId == null && anotherContact.contactId == null) {
            return ObjectUtils.equals(this.fullName, anotherContact.fullName)
                    && ObjectUtils.equals(this.friendlyName, anotherContact.friendlyName)
                    && ObjectUtils.equals(this.email, anotherContact.email)
                    && ObjectUtils.equals(this.phoneNumber, anotherContact.phoneNumber)
                    && ObjectUtils.equals(this.title, anotherContact.title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.contactId != null) {
            return this.contactId.hashCode();
        }
        int hashCode = 0;
        if (!StringUtils.isEmpty(this.fullName)) {
            hashCode += this.fullName.hashCode();
        }
        if (!StringUtils.isEmpty(this.friendlyName)) {
            hashCode += this.friendlyName.hashCode();
        }
        if (!StringUtils.isEmpty(this.email)) {
            hashCode += this.email.hashCode();
        }
        if (!StringUtils.isEmpty(this.phoneNumber)) {
            hashCode += this.phoneNumber.hashCode();
        }
        if (!StringUtils.isEmpty(this.title)) {
            hashCode += this.title.hashCode();
        }
        return hashCode;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(final Long contactId) {
        this.contactId = contactId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("[Contact domain object: [Id: %d] [Full Name: %s] [Friendly Name: %s] [Email: %s],"
                + "[Phone Number: %s], [Title: %s]]", this.getContactId(), this.getFullName(), this.getFriendlyName(),
                this.getEmail(), this.getPhoneNumber(), this.getTitle());

    }
}
