package gov.healthit.chpl.entity.surveillance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_surveillance_nonconformity")
public class PendingSurveillanceNonconformityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pending_surveillance_requirement_id")
    private Long pendingSurveillanceRequirementId;

    @Column(name = "nonconformity_type")
    private String type;

    @Column(name = "nonconformity_status")
    private String status;

    @Column(name = "date_of_determination")
    private Date dateOfDetermination;

    @Column(name = "corrective_action_plan_approval_date")
    private Date capApproval;

    @Column(name = "corrective_action_start_date")
    private Date capStart;

    @Column(name = "corrective_action_must_complete_date")
    private Date capMustCompleteDate;

    @Column(name = "corrective_action_end_date")
    private Date capEndDate;

    @Column(name = "summary")
    private String summary;

    @Column(name = "findings")
    private String findings;

    @Column(name = "sites_passed")
    private Integer sitesPassed;

    @Column(name = "total_sites")
    private Integer totalSites;

    @Column(name = "developer_explanation")
    private String developerExplanation;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingSurveillanceRequirementId() {
        return pendingSurveillanceRequirementId;
    }

    public void setPendingSurveillanceRequirementId(final Long pendingSurveillanceRequirementId) {
        this.pendingSurveillanceRequirementId = pendingSurveillanceRequirementId;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Date getDateOfDetermination() {
        return Util.getNewDate(dateOfDetermination);
    }

    public void setDateOfDetermination(final Date dateOfDetermination) {
        this.dateOfDetermination = Util.getNewDate(dateOfDetermination);
    }

    public Date getCapApproval() {
        return Util.getNewDate(capApproval);
    }

    public void setCapApproval(final Date capApproval) {
        this.capApproval = Util.getNewDate(capApproval);
    }

    public Date getCapStart() {
        return Util.getNewDate(capStart);
    }

    public void setCapStart(final Date capStart) {
        this.capStart = Util.getNewDate(capStart);
    }

    public Date getCapMustCompleteDate() {
        return Util.getNewDate(capMustCompleteDate);
    }

    public void setCapMustCompleteDate(final Date capMustCompleteDate) {
        this.capMustCompleteDate = Util.getNewDate(capMustCompleteDate);
    }

    public Date getCapEndDate() {
        return Util.getNewDate(capEndDate);
    }

    public void setCapEndDate(final Date capEndDate) {
        this.capEndDate = Util.getNewDate(capEndDate);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(final String findings) {
        this.findings = findings;
    }

    public Integer getSitesPassed() {
        return sitesPassed;
    }

    public void setSitesPassed(final Integer sitesPassed) {
        this.sitesPassed = sitesPassed;
    }

    public Integer getTotalSites() {
        return totalSites;
    }

    public void setTotalSites(final Integer totalSites) {
        this.totalSites = totalSites;
    }

    public String getDeveloperExplanation() {
        return developerExplanation;
    }

    public void setDeveloperExplanation(final String developerExplanation) {
        this.developerExplanation = developerExplanation;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
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
}
