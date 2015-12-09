package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

public class CorrectiveActionPlanDTO {

	private Long id;
	private Long certifiedProductId;
	private String acbSummary;
	private String developerSummary;
	private Date approvalDate;
	private Date effectiveDate;
	private Date estimatedCompletionDate;
	private Date actualCompletionDate;
	private Date noncomplainceDate;
	private String resolution;

	public CorrectiveActionPlanDTO() {
	}
	
	public CorrectiveActionPlanDTO(CorrectiveActionPlanEntity entity) {
		setId(entity.getId());
		setCertifiedProductId(entity.getCertifiedProductId());
		setAcbSummary(entity.getAcbSummaryDescription());
		setDeveloperSummary(entity.getDeveloperSummaryDescription());
		setApprovalDate(entity.getApprovalDate());
		setEffectiveDate(entity.getEffectiveDate());
		setEstimatedCompletionDate(entity.getEstimatedCompletionDate());
		setActualCompletionDate(entity.getActualCompletionDate());
		setNoncomplainceDate(entity.getNoncomplainceDeterminationDate());
		setResolution(entity.getResolution());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAcbSummary() {
		return acbSummary;
	}

	public void setAcbSummary(String acbSummary) {
		this.acbSummary = acbSummary;
	}

	public String getDeveloperSummary() {
		return developerSummary;
	}

	public void setDeveloperSummary(String developerSummary) {
		this.developerSummary = developerSummary;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
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

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getEstimatedCompletionDate() {
		return estimatedCompletionDate;
	}

	public void setEstimatedCompletionDate(Date estimatedCompleteionDate) {
		this.estimatedCompletionDate = estimatedCompleteionDate;
	}

	public Date getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(Date actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
	}

	public Date getNoncomplainceDate() {
		return noncomplainceDate;
	}

	public void setNoncomplainceDate(Date noncomplainceDate) {
		this.noncomplainceDate = noncomplainceDate;
	}
}
