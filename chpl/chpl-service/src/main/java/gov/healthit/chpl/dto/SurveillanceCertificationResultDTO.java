package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.SurveillanceCertificationResultEntity;

public class SurveillanceCertificationResultDTO {

	private Long id;
	private Long surveillanceId;
	private CertificationCriterionDTO certCriterion;
	private int numSites;
	private String passRate;
	private String results;
	
	public SurveillanceCertificationResultDTO() {
	}
	
	public SurveillanceCertificationResultDTO(SurveillanceCertificationResultEntity entity) {
		setId(entity.getId());
		if(entity.getSurveillance() != null) {
			setSurveillanceId(entity.getSurveillance().getId());
		}
		setNumSites(entity.getNumSites());
		setPassRate(entity.getPassRate());
		setResults(entity.getResults());
		setCertCriterion(new CertificationCriterionDTO(entity.getCertCriterion()));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSurveillanceId() {
		return surveillanceId;
	}

	public void setSurveillanceId(Long surveillanceId) {
		this.surveillanceId = surveillanceId;
	}

	public CertificationCriterionDTO getCertCriterion() {
		return certCriterion;
	}

	public void setCertCriterion(CertificationCriterionDTO certCriterion) {
		this.certCriterion = certCriterion;
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

	public String getResults() {
		return results;
	}

	public void setResults(String resolution) {
		this.results = resolution;
	}
}
