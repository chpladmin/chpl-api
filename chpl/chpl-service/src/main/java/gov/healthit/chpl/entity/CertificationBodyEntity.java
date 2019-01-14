package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

/**
 * Certification body mapping to database.
 * @author kekey
 *
 */
@Entity
@Table(name = "certification_body")
public class CertificationBodyEntity implements Serializable {
    private static final long serialVersionUID = -4603773689327950041L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_body_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true)
    private AddressEntity address;

    @Column(name = "acb_code")
    private String acbCode;

    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "website", nullable = true)
    private String website;

    @Column(name = "retired", nullable = false)
    private Boolean retired;

    @Basic(optional = false)
    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long long1) {
        this.id = long1;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    public String getAcbCode() {
        return acbCode;
    }

    public void setAcbCode(final String acbCode) {
        this.acbCode = acbCode;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(Boolean retired) {
        this.retired = retired;
    }

}
