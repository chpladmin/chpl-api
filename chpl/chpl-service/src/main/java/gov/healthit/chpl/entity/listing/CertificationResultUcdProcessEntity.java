package gov.healthit.chpl.entity.listing;

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

import gov.healthit.chpl.entity.UcdProcessEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "certification_result_ucd_process")
public class CertificationResultUcdProcessEntity implements Serializable {
    private static final long serialVersionUID = -8570212776898137339L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_result_ucd_process_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "ucd_process_id")
    private Long ucdProcessId;

    @Column(name = "ucd_process_details")
    private String ucdProcessDetails;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "ucd_process_id", unique = true, nullable = true, insertable = false, updatable = false)
    private UcdProcessEntity ucdProcess;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationResultId() {
        return certificationResultId;
    }

    public void setCertificationResultId(final Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    protected Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    protected Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    protected Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

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

    public Long getUcdProcessId() {
        return ucdProcessId;
    }

    public void setUcdProcessId(final Long ucdProcessId) {
        this.ucdProcessId = ucdProcessId;
    }

    public String getUcdProcessDetails() {
        return ucdProcessDetails;
    }

    public void setUcdProcessDetails(final String ucdProcessDetails) {
        this.ucdProcessDetails = ucdProcessDetails;
    }

    public UcdProcessEntity getUcdProcess() {
        return ucdProcess;
    }

    public void setUcdProcess(final UcdProcessEntity ucdProcess) {
        this.ucdProcess = ucdProcess;
    }
}
