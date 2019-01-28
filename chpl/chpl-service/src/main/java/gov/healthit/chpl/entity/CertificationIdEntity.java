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
import javax.persistence.Transient;

import gov.healthit.chpl.util.Util;

/**
 * Object mapping for hibernate-handled table: product. Table to store products
 * that are submitted for developers
 *
 * @author
 */

@Entity
@Table(name = "ehr_certification_id")
public class CertificationIdEntity implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ehr_certification_id_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "certification_id", length = 255, nullable = false)
    private String certificationId;

    @Basic(optional = false)
    @Column(name = "key", nullable = false)
    private String key;

    @Basic(optional = true)
    @Column(name = "practice_type_id", nullable = true)
    private Long practiceTypeId;

    @Basic(optional = false)
    @Column(name = "year", nullable = false)
    private String year;

    /**
     * Default constructor, mainly for hibernate use.
     */
    public CertificationIdEntity() {
        // Default constructor
    }

    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public CertificationIdEntity(Long id) {
        this.id = id;
    }

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    @Transient
    public Class<?> getClassType() {
        return CertificationIdEntity.class;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
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
     * Return the value associated with the column: certification_id.
     *
     * @return A String object (this.certificationId)
     */
    public String getCertificationId() {
        return this.certificationId;
    }

    /**
     * Set the value related to the column: certification_id.
     * @param certId the certificationId value you wish to set
     */
    public void setCertificationId(final String certId) {
        this.certificationId = certId;
    }

    /**
     * Return the value associated with the column: key.
     * @return A String object (this.key)
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the value related to the column: key.
     * @param key
     *            the key value you wish to set
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Return the value associated with the column: practice_type_id.
     *
     * @return A Long object (this.practiceTypeId)
     */
    public Long getPracticeTypeId() {
        return this.practiceTypeId;
    }

    /**
     * Set the value related to the column: practice_type_id.
     *
     * @param practiceTypeId
     *            the practiceTypeId value you wish to set
     */
    public void setPracticeTypeId(final Long practiceTypeId) {
        this.practiceTypeId = practiceTypeId;
    }

    /**
     * Return the value associated with the column: year.
     *
     * @return A String object (this.year)
     */
    public String getYear() {
        return this.year;
    }

    /**
     * Set the value related to the column: year.
     *
     * @param year
     *            the year value you wish to set
     */
    public void setYear(final String year) {
        this.year = year;
    }

    /**
     * Provides toString implementation.
     *
     * @see java.lang.Object#toString()
     * @return String representation of this class.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("creationDate: " + this.getCreationDate() + ", ");
        sb.append("id: " + this.getId() + ", ");
        sb.append("key: " + this.getKey() + ", ");
        sb.append("lastModifiedDate: " + this.getLastModifiedDate() + ", ");
        sb.append("lastModifiedUser: " + this.getLastModifiedUser() + ", ");
        sb.append("certificationId: " + this.getCertificationId() + ", ");
        sb.append("year: " + this.getYear() + ", ");
        sb.append("practiceTypeId: " + this.getPracticeTypeId() + ", ");
        return sb.toString();
    }

}
