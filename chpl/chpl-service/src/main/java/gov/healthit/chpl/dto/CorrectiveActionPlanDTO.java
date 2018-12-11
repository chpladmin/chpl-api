package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;
import gov.healthit.chpl.util.Util;

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
        return Util.getNewDate(approvalDate);
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = Util.getNewDate(approvalDate);
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

    public Date getActualCompletionDate() {
        return Util.getNewDate(actualCompletionDate);
    }

    public void setActualCompletionDate(final Date actualCompletionDate) {
        this.actualCompletionDate = Util.getNewDate(actualCompletionDate);
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
