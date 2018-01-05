package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pending_certification_result_ucd_process")
public class PendingCertificationResultUcdProcessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_certification_result_ucd_process_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Long pendingCertificationResultId;

    @Column(name = "ucd_process_id")
    private Long ucdProcessId;

    @Column(name = "ucd_process_name")
    private String ucdProcessName;
    
    @Column(name = "fuzzy_match_ucd_process_name")
    private String fuzzyMatchUcdProcessName;

    @Column(name = "ucd_process_details")
    private String ucdProcessDetails;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getPendingCertificationResultId() {
        return pendingCertificationResultId;
    }

    public void setPendingCertificationResultId(final Long pendingCertificationResultId) {
        this.pendingCertificationResultId = pendingCertificationResultId;
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

    public String getUcdProcessName() {
        return ucdProcessName;
    }

    public void setUcdProcessName(final String ucdProcessName) {
        this.ucdProcessName = ucdProcessName;
    }

	public String getFuzzyMatchUcdProcessName() {
		return fuzzyMatchUcdProcessName;
	}

	public void setFuzzyMatchUcdProcessName(String fuzzyMatchUcdProcessName) {
		this.fuzzyMatchUcdProcessName = fuzzyMatchUcdProcessName;
	}
}
