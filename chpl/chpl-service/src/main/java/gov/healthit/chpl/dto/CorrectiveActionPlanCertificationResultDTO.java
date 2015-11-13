package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CorrectiveActionPlanCertificationEntity;

public class CorrectiveActionPlanCertificationResultDTO {

	private Long id;
	private CertificationCriterionDTO certCriterion;
	private String acbSummary;
	private String developerSummary;
	private String resolution;
	
	public CorrectiveActionPlanCertificationResultDTO() {
		
	}
	
	public CorrectiveActionPlanCertificationResultDTO(CorrectiveActionPlanCertificationEntity entity) {
		setId(entity.getId());
		setAcbSummary(entity.getAcbSummary());
		setDeveloperSummary(entity.getDeveloperSummaryDescription());
		setResolution(entity.getResolution());
		setCertCriterion(new CertificationCriterionDTO(entity.getCertificationCriterion()));
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
}
