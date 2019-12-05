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
import javax.validation.constraints.Size;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "product")
public class ProductEntitySimple implements Serializable {
    private static final long serialVersionUID = -533208090008462551L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = true)
    @Column(name = "report_file_location", length = 255)
    private String reportFileLocation;

    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long developerId;

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
     * Return the value associated with the column: reportFileLocation.
     *
     * @return A String object (this.reportFileLocation)
     */
    public String getReportFileLocation() {
        return this.reportFileLocation;

    }

    /**
     * Set the value related to the column: reportFileLocation.
     *
     * @param reportFileLocation
     *            the reportFileLocation value you wish to set
     */
    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    /**
     * Return the value associated with the column: developer.
     *
     * @return A Developer object (this.developer)
     */
    public Long getDeveloperId() {
        return this.developerId;

    }

    /**
     * Set the value related to the column: developer.
     * @param developerId the developer value you wish to set
     */
    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(final Long contactId) {
        this.contactId = contactId;
    }
}
