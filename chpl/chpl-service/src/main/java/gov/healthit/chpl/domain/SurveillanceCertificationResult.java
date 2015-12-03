package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;

public class SurveillanceCertificationResult {
	
	private Long id;
	private Long certificationCriterionId;
	private String certificationCriterionNumber;
	private String certificationCriterionTitle;
	private int numSites;
	private String passRate;
	private String result;
	
	public SurveillanceCertificationResult() {}
	public SurveillanceCertificationResult(SurveillanceCertificationResultDTO dto) {
		this.id = dto.getId();
		if(dto.getCertCriterion() != null) {
			this.certificationCriterionId = dto.getCertCriterion().getId();
			this.certificationCriterionNumber = dto.getCertCriterion().getNumber();
			this.certificationCriterionTitle = dto.getCertCriterion().getTitle();
		}
		this.numSites = dto.getNumSites();
		this.passRate = dto.getPassRate();
		this.result = dto.getResults();
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
	public int getNumSites() {
		return numSites;
	}
	public void setNumSites(int numSites) {
		this.numSites = numSites;
	}
	public String getPassRate() {
		return passRate;
	}
	public void setPassRate(String passRate) {
		this.passRate = passRate;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
