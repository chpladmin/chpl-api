package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CorrectiveActionPlanCertificationEntity;

public class CorrectiveActionPlanCertificationResultDTO {

	private Long id;
	private Long correctiveActionPlanId;
	private CertificationCriterionDTO certCriterion;
	private String acbSummary;
	private String developerSummary;
	private String resolution;
	private String surveillancePassRate;
	private String surveillanceResults;
	
	public CorrectiveActionPlanCertificationResultDTO() {
		
	}
	
	public CorrectiveActionPlanCertificationResultDTO(CorrectiveActionPlanCertificationEntity entity) {
		setId(entity.getId());
		if(entity.getCorrectiveActionPlan() != null) {
			setCorrectiveActionPlanId(entity.getCorrectiveActionPlan().getId());
		}
		setAcbSummary(entity.getAcbSummary());
		setDeveloperSummary(entity.getDeveloperSummaryDescription());
		setResolution(entity.getResolution());
		setCertCriterion(new CertificationCriterionDTO(entity.getCertificationCriterion()));
		setSurveillancePassRate(entity.getSurveillancePassRate());
		setSurveillanceResults(entity.getSurveillanceResults());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CertificationCriterionDTO getCertCriterion() {
		return certCriterion;
	}

	public void setCertCriterion(CertificationCriterionDTO certCriterion) {
		this.certCriterion = certCriterion;
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

	public Long getCorrectiveActionPlanId() {
		return correctiveActionPlanId;
	}

	public void setCorrectiveActionPlanId(Long correctiveActionPlanId) {
		this.correctiveActionPlanId = correctiveActionPlanId;
	}

	public String getSurveillancePassRate() {
		return surveillancePassRate;
	}

	public void setSurveillancePassRate(String surveillancePassRate) {
		this.surveillancePassRate = surveillancePassRate;
	}

	public String getSurveillanceResults() {
		return surveillanceResults;
	}

	public void setSurveillanceResults(String surveillanceResults) {
		this.surveillanceResults = surveillanceResults;
	}
}
