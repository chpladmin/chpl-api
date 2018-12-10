package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

/**
 * Object mapping for hibernate-handled table: contact.
 *
 * @author auto-generated / cwatson
 */
@Entity
@Table(name = "contact")
public class ContactEntity implements Serializable {
    private static final long serialVersionUID = 1586086005459839264L;
    private static final int NAME_COMPONENT_LENGTH = 250;
    private static final int FULL_NAME_LENGTH = 500;
    private static final int PHONE_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "contact_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = true)
    @Column(length = NAME_COMPONENT_LENGTH)
    private String email;

    @Basic(optional = false)
    @Column(name = "full_name", nullable = false, length = FULL_NAME_LENGTH)
    private String fullName;

    @Basic(optional = true)
    @Column(name = "friendly_name", length = NAME_COMPONENT_LENGTH)
    private String friendlyName;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @Column(name = "phone_number", length = PHONE_LENGTH)
    private String phoneNumber;

    @Basic(optional = true)
    @Column(name = "signature_date")
    private Date signatureDate;

    @Basic(optional = true)
    @Column(length = NAME_COMPONENT_LENGTH)
    private String title;

    /**
     * Default constructor, mainly for hibernate use.
     */
    public ContactEntity() { }

    /**
     * Constructed with an ID.
     * @param id the id
     */
    public ContactEntity(final Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public Long getId() {
        return this.id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return this.lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getSignatureDate() {
        return Util.getNewDate(this.signatureDate);
    }


    public void setSignatureDate(final Date signatureDate) {
        this.signatureDate = Util.getNewDate(signatureDate);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("[Contact Entity: [Id: %d] [Full Name: %s] [Friendly Name: %s] [Email: %s],"
                + "[Phone Number: %s], [Title: %s]]", this.getId(), this.getFullName(), this.getFriendlyName(),
                this.getEmail(), this.getPhoneNumber(), this.getTitle());

    }
}
