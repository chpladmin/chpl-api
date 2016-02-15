package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;

public class CorrectiveActionPlanCertificationResult {
	
	private Long id;
	private Long certificationCriterionId;
	private String certificationCriterionNumber;
	private String certificationCriterionTitle;
	private String summary;
	private String developerExplanation;
	private String resolution;
	private Integer surveillancePassRate;
	private Integer surveillanceSiteCount;
	
	public CorrectiveActionPlanCertificationResult() {}
	public CorrectiveActionPlanCertificationResult(CorrectiveActionPlanCertificationResultDTO dto) {
		this.id = dto.getId();
		if(dto.getCertCriterion() != null) {
			this.certificationCriterionId = dto.getCertCriterion().getId();
			this.certificationCriterionNumber = dto.getCertCriterion().getNumber();
			this.certificationCriterionTitle = dto.getCertCriterion().getTitle();
		}
		this.summary = dto.getSummary();
		this.developerExplanation = dto.getDeveloperExplanation();
		this.resolution = dto.getResolution();
		this.surveillancePassRate = dto.getNumSitesPassed();
		this.surveillanceSiteCount = dto.getNumSitesTotal();
	}
		
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}
	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}
	public String getCertificationCriterionNumber() {
		return certificationCriterionNumber;
	}
	public void setCertificationCriterionNumber(String certificationCriterionNumber) {
		this.certificationCriterionNumber = certificationCriterionNumber;
	}
	public String getCertificationCriterionTitle() {
		return certificationCriterionTitle;
	}
	public void setCertificationCriterionTitle(String certificationCriterionTitle) {
		this.certificationCriterionTitle = certificationCriterionTitle;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDeveloperExplanation() {
		return developerExplanation;
	}
	public void setDeveloperExplanation(String developerExplanation) {
		this.developerExplanation = developerExplanation;
	}
	public Integer getSurveillancePassRate() {
		return surveillancePassRate;
	}
	public void setSurveillancePassRate(Integer surveillancePassRate) {
		this.surveillancePassRate = surveillancePassRate;
	}
	public Integer getSurveillanceSiteCount() {
		return surveillanceSiteCount;
	}
	public void setSurveillanceSiteCount(Integer surveillanceSiteCount) {
		this.surveillanceSiteCount = surveillanceSiteCount;
	}	
}
