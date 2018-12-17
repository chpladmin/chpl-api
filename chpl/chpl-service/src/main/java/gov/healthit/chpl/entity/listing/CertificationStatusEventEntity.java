package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationStatusEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "certification_status_event")
public class CertificationStatusEventEntity implements Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = 4174889617079658144L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_status_event_id")
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "event_date")
    private Date eventDate;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_status_id", insertable = false, updatable = false)
    private CertificationStatusEntity certificationStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getEventDate() {
        return Util.getNewDate(eventDate);
    }

    public void setEventDate(final Date eventDate) {
        this.eventDate = Util.getNewDate(eventDate);
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

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(final Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public CertificationStatusEntity getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final CertificationStatusEntity certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
