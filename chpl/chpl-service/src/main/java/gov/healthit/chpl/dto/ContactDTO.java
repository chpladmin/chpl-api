package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ContactEntity;

/**
 * Data transfer object for Contacts.
 * @author alarned
 *
 */
public class ContactDTO implements Serializable {
    private static final long serialVersionUID = 5417465972193498436L;
    private Long id;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Date signatureDate;

    /**
     * Default constructor.
     */
    public ContactDTO() {
    }

    /**
     * Constructed from an entity.
     * @param entity the entity
     */
    public ContactDTO(final ContactEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.fullName = entity.getFullName();
            this.friendlyName = entity.getFriendlyName();
            this.email = entity.getEmail();
            this.phoneNumber = entity.getPhoneNumber();
            this.title = entity.getTitle();
            this.signatureDate = entity.getSignatureDate();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(final Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    @Override
    public String toString() {
        return String.format("[Contact DTO: [Id: {}] [Full Name: {}] [Friendly Name: {}] [Email: {}],"
                + "[Phone Number: {}], [Title: {}]]", this.getId(), this.getFullName(), this.getFriendlyName(),
                this.getEmail(), this.getPhoneNumber(), this.getTitle());

    }
}
