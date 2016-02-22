package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

public class CorrectiveActionPlanDTO {

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
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	public Date getSurveillanceStartDate() {
		return surveillanceStartDate;
	}

	public void setSurveillanceStartDate(Date surveillanceStartDate) {
		this.surveillanceStartDate = surveillanceStartDate;
	}

	public Date getSurveillanceEndDate() {
		return surveillanceEndDate;
	}

	public void setSurveillanceEndDate(Date surveillanceEndDate) {
		this.surveillanceEndDate = surveillanceEndDate;
	}

	public Boolean getSurveillanceResult() {
		return surveillanceResult;
	}

	public void setSurveillanceResult(Boolean surveillanceResult) {
		this.surveillanceResult = surveillanceResult;
	}

	public Date getNonComplianceDeterminationDate() {
		return nonComplianceDeterminationDate;
	}

	public void setNonComplianceDeterminationDate(Date nonComplianceDeterminationDate) {
		this.nonComplianceDeterminationDate = nonComplianceDeterminationDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getRequiredCompletionDate() {
		return requiredCompletionDate;
	}

	public void setRequiredCompletionDate(Date requiredCompletionDate) {
		this.requiredCompletionDate = requiredCompletionDate;
	}

	public Date getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(Date actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
	}

}
