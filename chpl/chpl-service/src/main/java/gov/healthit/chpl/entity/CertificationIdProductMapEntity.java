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
@Table(name = "ehr_certification_id_product_map")
public class CertificationIdProductMapEntity implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ehr_certification_id_product_map_id", nullable = false)
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
    @Column(name = "ehr_certification_id_id", nullable = false)
    private Long certificationIdId;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    /**
     * Default constructor, mainly for hibernate use.
     */
    public CertificationIdProductMapEntity() {
        // Default constructor
    }

    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public CertificationIdProductMapEntity(Long id) {
        this.id = id;
    }

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    @Transient
    public Class<?> getClassType() {
        return CertificationIdProductMapEntity.class;
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
     * Return the value associated with the column: certified_product_id.
     *
     * @return A Long object (this.certifiedProductId)
     */
    public Long getCertifiedProductId() {
        return this.certifiedProductId;
    }

    /**
     * Set the value related to the column: product_id.
     *
     * @param certifiedProductId
     *            the certifiedProductId value you wish to set
     */
    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    /**
     * Return the value associated with the column: ehr_certification_id_id.
     *
     * @return A Long object (this.certificationId)
     */
    public Long getCertificationIdId() {
        return this.certificationIdId;
    }

    /**
     * Set the value related to the column: ehr_certification_id_id.
     *
     * @param certificationIdId
     *            the certificationIdId value you wish to set
     */
    public void setCertificationIdId(final Long certificationIdId) {
        this.certificationIdId = certificationIdId;
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
        sb.append("lastModifiedDate: " + this.getLastModifiedDate() + ", ");
        sb.append("lastModifiedUser: " + this.getLastModifiedUser() + ", ");
        sb.append("certifiedProductId: " + this.getCertifiedProductId() + ", ");
        sb.append("certificationIdId: " + this.getCertificationIdId() + ", ");
        return sb.toString();
    }
}
