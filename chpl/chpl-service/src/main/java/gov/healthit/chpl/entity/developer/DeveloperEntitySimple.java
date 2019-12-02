package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "vendor")
public class DeveloperEntitySimple implements Serializable {

    private static final long serialVersionUID = -1396979119499564864L;

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
    @Column(length = 300, nullable = true)
    private String website;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

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

    /**
     * Default constructor, mainly for hibernate use.
     */
    public DeveloperEntitySimple() {
        // Default constructor
    }

    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public DeveloperEntitySimple(Long id) {
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
    public DeveloperEntitySimple(Date creationDate, Boolean deleted, Long id, Date lastModifiedDate, Long lastModifiedUser) {

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
        return DeveloperEntitySimple.class;
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

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
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
