package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ContactDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Domain object representing a Contact. Can be used as either contact information for a product / developer, or as
 * contact information for a user.
 * @author alarned
 *
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class Contact implements Serializable {
    private static final long serialVersionUID = 5378524206189674741L;
    private static final Logger LOGGER = LogManager.getLogger(Contact.class);

    /**
     * Contact internal ID
     */
    private Long contactId;

    /**
     * Contact full name. This variable is applicable for 2014 and 2015
     * Edition, and a string variable that does not take any restrictions on
     * formatting or values.
     */
    private String fullName;

    /**
     * Contact friendly name. This variable is applicable for 2014 and 2015 Edition,
     * and a string variable that does not take any restrictions on formatting
     * or values. Usually only used for Contacts representing Users.
     */
    @Deprecated
    private String friendlyName;

    /**
     * Email address of the contact. It is applicable to 2014 and 2015 Edition.
     */
    private String email;

    /**
     * Phone number of health IT developer contact. This variable is applicable
     * for 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values. Do not include hyphens.
     */
    private String phoneNumber;

    /**
     * Contact title (Ms., Mr., Dr., etc)
     */
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
    public Contact(ContactDTO dto) {
        this.contactId = dto.getId();
        this.fullName = dto.getFullName();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
        this.title = dto.getTitle();
    }

    public Contact(HashMap<String, Object> map) {
        if (map.containsKey("contactId") && map.get("contactId") != null) {
            try {
                this.contactId = Long.parseLong(map.get("contactId").toString());
            } catch (NumberFormatException ex) {
                LOGGER.warn("contactId in map = '" + map.get("contactId") + "' is not parseable into a Long");
            }
        }
        if (map.containsKey("fullName") && map.get("fullName") != null) {
            this.fullName = map.get("fullName").toString();
        }
        if (map.containsKey("email") && map.get("email") != null) {
            this.email = map.get("email").toString();
        }
        if (map.containsKey("phoneNumber") && map.get("phoneNumber") != null) {
            this.phoneNumber = map.get("phoneNumber").toString();
        }
        if (map.containsKey("title") && map.get("title") != null) {
            this.title = map.get("title").toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contact)) {
            return false;
        }
        Contact anotherContact = (Contact) obj;
        if ((this.contactId != null && anotherContact.contactId != null
                && this.contactId.longValue() == anotherContact.contactId.longValue())
                || (this.contactId == null && anotherContact.contactId == null)) {
            return ObjectUtils.equals(this.fullName, anotherContact.fullName)
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
        return String.format("[Contact domain object: [Id: %d] [Full Name: %s] [Email: %s],"
                + "[Phone Number: %s], [Title: %s]]", this.getContactId(), this.getFullName(),
                this.getEmail(), this.getPhoneNumber(), this.getTitle());

    }
}
