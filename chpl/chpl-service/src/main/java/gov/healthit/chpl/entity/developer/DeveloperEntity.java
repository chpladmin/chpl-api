package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.util.Util;

@Entity
@DynamicUpdate
@Table(name = "vendor")
public class DeveloperEntity implements Serializable {

    private static final long serialVersionUID = -1396979009499564864L;
    private static final int WEBSITE_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long id;

    @Column(name = "vendor_code", insertable = false, updatable = false)
    private String developerCode;

    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(length = WEBSITE_MAX_LENGTH, nullable = true)
    private String website;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true)
    @Where(clause = "deleted <> 'true'")
    private AddressEntity address;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ContactEntity contact;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @MapsId("id")
    @JoinColumn(name = "vendor_id", unique = true, nullable = true, insertable = false, updatable = false)
    private DeveloperCertificationStatusesEntity developerCertificationStatuses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "developerId")
    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<DeveloperStatusEventEntity> statusEvents = new LinkedHashSet<DeveloperStatusEventEntity>();

    /**
     * Default constructor, mainly for hibernate use.
     */
    public DeveloperEntity() {
        // Default constructor
    }

    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public DeveloperEntity(Long id) {
        this.id = id;
    }

    /**
     * Constructor taking a given ID.
     *
     * @param creationDate
     *            Date object;
     * @param deleted
     *            Boolean object;
     * @param id
     *            Long object;
     * @param lastModifiedDate
     *            Date object;
     * @param lastModifiedUser
     *            Long object;
     */
    public DeveloperEntity(Date creationDate, Boolean deleted, Long id, Date lastModifiedDate, Long lastModifiedUser) {

        this.creationDate = creationDate;
        this.deleted = deleted;
        this.id = id;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifiedUser = lastModifiedUser;
    }

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    @Transient
    public Class<?> getClassType() {
        return DeveloperEntity.class;
    }

    /**
     * Return the value associated with the column: address.
     *
     * @return A Address object (this.address)
     */
    public AddressEntity getAddress() {
        return this.address;
    }

    /**
     * Set the value related to the column: address.
     *
     * @param address
     *            the address value you wish to set
     */
    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    /**
     * Return the value associated with the column: deleted.
     *
     * @return A Boolean object (this.deleted)
     */
    public Boolean isDeleted() {
        return this.deleted;

    }

    /**
     * Set the value related to the column: deleted.
     *
     * @param deleted
     *            the deleted value you wish to set
     */
    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Return the value associated with the column: id.
     *
     * @return A Long object (this.id)
     */
    public Long getId() {
        return this.id;

    }

    /**
     * Set the value related to the column: id.
     *
     * @param id
     *            the id value you wish to set
     */
    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    /**
     * Return the value associated with the column: lastModifiedUser.
     *
     * @return A Long object (this.lastModifiedUser)
     */
    public Long getLastModifiedUser() {
        return this.lastModifiedUser;

    }

    /**
     * Set the value related to the column: lastModifiedUser.
     *
     * @param lastModifiedUser
     *            the lastModifiedUser value you wish to set
     */
    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    /**
     * Return the value associated with the column: name.
     *
     * @return A String object (this.name)
     */
    public String getName() {
        return this.name;

    }

    /**
     * Set the value related to the column: name.
     *
     * @param name
     *            the name value you wish to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Return the value associated with the column: website.
     *
     * @return A String object (this.website)
     */
    public String getWebsite() {
        return this.website;

    }

    /**
     * Set the value related to the column: website.
     *
     * @param website
     *            the website value you wish to set
     */
    public void setWebsite(final String website) {
        this.website = website;
    }

    public Boolean getSelfDeveloper() {
        return selfDeveloper;
    }

    public void setSelfDeveloper(Boolean selfDeveloper) {
        this.selfDeveloper = selfDeveloper;
    }

    public DeveloperCertificationStatusesEntity getDeveloperCertificationStatusesEntity() {
        return this.developerCertificationStatuses;
    }

    public void setDeveloperCertificationStatuses(final
            DeveloperCertificationStatusesEntity developerCertificationStatusesEntity) {
        this.developerCertificationStatuses = developerCertificationStatusesEntity;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public ContactEntity getContact() {
        return contact;
    }

    public void setContact(final ContactEntity contact) {
        this.contact = contact;
    }

    public Set<DeveloperStatusEventEntity> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(final Set<DeveloperStatusEventEntity> statusEvents) {
        this.statusEvents = statusEvents;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("creationDate: " + this.getCreationDate() + ", ");
        sb.append("deleted: " + this.isDeleted() + ", ");
        sb.append("id: " + this.getId() + ", ");
        sb.append("lastModifiedDate: " + this.getLastModifiedDate() + ", ");
        sb.append("lastModifiedUser: " + this.getLastModifiedUser() + ", ");
        sb.append("name: " + this.getName() + ", ");
        sb.append("website: " + this.getWebsite());
        return sb.toString();
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(final Long contactId) {
        this.contactId = contactId;
    }

}
