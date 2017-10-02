package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

public class CorrectiveActionPlanDTO implements Serializable {
    private static final long serialVersionUID = 489989180361599764L;
    private Long id;
    private Long certifiedProductId;
    private Date surveillanceStartDate;
    private Date surveillanceEndDate;
    private Boolean surveillanceResult;
    private Date nonComplianceDeterminationDate;
    private Date approvalDate;
    private Date startDate;
    private Date requiredCompletionDate;
    private Date actualCompletionDate;
    private String summary;
    private String developerExplanation;
    private String resolution;

    public CorrectiveActionPlanDTO() {
    }

    public CorrectiveActionPlanDTO(CorrectiveActionPlanEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.surveillanceStartDate = entity.getSurveillanceStartDate();
        this.surveillanceEndDate = entity.getSurveillanceEndDate();
        this.surveillanceResult = entity.getSurveillanceResult();
        this.nonComplianceDeterminationDate = entity.getNonComplianceDeterminationDate();
        this.approvalDate = entity.getApprovalDate();
        this.startDate = entity.getStartDate();
        this.requiredCompletionDate = entity.getRequiredCompletionDate();
        this.actualCompletionDate = entity.getActualCompletionDate();
        this.summary = entity.getSummary();
        this.developerExplanation = entity.getDeveloperExplanation();
        this.resolution = entity.getResolution();
    }

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
        return approvalDate;
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Date getSurveillanceStartDate() {
        return surveillanceStartDate;
    }

    public void setSurveillanceStartDate(final Date surveillanceStartDate) {
        this.surveillanceStartDate = surveillanceStartDate;
    }

    public Date getSurveillanceEndDate() {
        return surveillanceEndDate;
    }

    public void setSurveillanceEndDate(final Date surveillanceEndDate) {
        this.surveillanceEndDate = surveillanceEndDate;
    }

    public Boolean getSurveillanceResult() {
        return surveillanceResult;
    }

    public void setSurveillanceResult(final Boolean surveillanceResult) {
        this.surveillanceResult = surveillanceResult;
    }

    public Date getNonComplianceDeterminationDate() {
        return nonComplianceDeterminationDate;
    }

    public void setNonComplianceDeterminationDate(final Date nonComplianceDeterminationDate) {
        this.nonComplianceDeterminationDate = nonComplianceDeterminationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getRequiredCompletionDate() {
        return requiredCompletionDate;
    }

    public void setRequiredCompletionDate(final Date requiredCompletionDate) {
        this.requiredCompletionDate = requiredCompletionDate;
    }

    public Date getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(final Date actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
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
