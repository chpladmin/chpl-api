package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "corrective_action_plan")
public class CorrectiveActionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "corrective_action_plan_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "surveillance_start")
    private Date surveillanceStartDate;

    @Column(name = "surveillance_end")
    private Date surveillanceEndDate;

    @Basic(optional = false)
    @Column(name = "surveillance_result")
    private Boolean surveillanceResult;

    @Basic(optional = false)
    @Column(name = "noncompliance_determination_date")
    private Date nonComplianceDeterminationDate;

    @Column(name = "approval_date")
    private Date approvalDate;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "completion_date_required")
    private Date requiredCompletionDate;

    @Column(name = "completion_date_actual")
    private Date actualCompletionDate;

    @Column(name = "summary")
    private String summary;

    @Column(name = "developer_explanation")
    private String developerExplanation;

    @Column(name = "resolution")
    private String resolution;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull()
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @NotNull()
    @Column(nullable = false)
    private Boolean deleted;

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

    public Date getApprovalDate() {
        return Util.getNewDate(approvalDate);
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = Util.getNewDate(approvalDate);
    }

    public Date getActualCompletionDate() {
        return Util.getNewDate(actualCompletionDate);
    }

    public void setActualCompletionDate(final Date actualCompletionDate) {
        this.actualCompletionDate = Util.getNewDate(actualCompletionDate);
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

    public Date getSurveillanceStartDate() {
        return Util.getNewDate(surveillanceStartDate);
    }

    public void setSurveillanceStartDate(final Date surveillanceStartDate) {
        this.surveillanceStartDate = Util.getNewDate(surveillanceStartDate);
    }

    public Date getSurveillanceEndDate() {
        return Util.getNewDate(surveillanceEndDate);
    }

    public void setSurveillanceEndDate(final Date surveillanceEndDate) {
        this.surveillanceEndDate = Util.getNewDate(surveillanceEndDate);
    }

    public Boolean getSurveillanceResult() {
        return surveillanceResult;
    }

    public void setSurveillanceResult(final Boolean surveillanceResult) {
        this.surveillanceResult = surveillanceResult;
    }

    public Date getNonComplianceDeterminationDate() {
        return Util.getNewDate(nonComplianceDeterminationDate);
    }

    public void setNonComplianceDeterminationDate(final Date nonComplianceDeterminationDate) {
        this.nonComplianceDeterminationDate = Util.getNewDate(nonComplianceDeterminationDate);
    }

    public Date getStartDate() {
        return Util.getNewDate(startDate);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = Util.getNewDate(startDate);
    }

    public Date getRequiredCompletionDate() {
        return Util.getNewDate(requiredCompletionDate);
    }

    public void setRequiredCompletionDate(final Date requiredCompletionDate) {
        this.requiredCompletionDate = Util.getNewDate(requiredCompletionDate);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
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
}
