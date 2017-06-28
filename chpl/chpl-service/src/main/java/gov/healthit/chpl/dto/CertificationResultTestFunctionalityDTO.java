package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestFunctionalityEntity;

public class CertificationResultTestFunctionalityDTO implements Serializable {
	private static final long serialVersionUID = 1504901339062574362L;
	private Long id;
	private Long certificationResultId;
	private Long testFunctionalityId;
	private String testFunctionalityName;
	private String testFunctionalityNumber;
	private String testFunctionalityEdition;
	private Boolean deleted;
	
	public CertificationResultTestFunctionalityDTO(){}
	
	public CertificationResultTestFunctionalityDTO(CertificationResultTestFunctionalityEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testFunctionalityId = entity.getTestFunctionalityId();
		if(entity.getTestFunctionality() != null) {
			this.testFunctionalityName = entity.getTestFunctionality().getName();
			this.testFunctionalityNumber = entity.getTestFunctionality().getNumber();
			if(entity.getTestFunctionality().getCertificationEdition() != null) {
				this.testFunctionalityEdition = entity.getTestFunctionality().getCertificationEdition().getYear();
			}
		}
		this.deleted = entity.getDeleted();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getTestFunctionalityId() {
		return testFunctionalityId;
	}

	public void setTestFunctionalityId(Long testFunctionalityId) {
		this.testFunctionalityId = testFunctionalityId;
	}

	public String getTestFunctionalityName() {
		return testFunctionalityName;
	}

	public void setTestFunctionalityName(String testFunctionalityName) {
		this.testFunctionalityName = testFunctionalityName;
	}

	public String getTestFunctionalityNumber() {
		return testFunctionalityNumber;
	}

	public void setTestFunctionalityNumber(String testFunctionalityNumber) {
		this.testFunctionalityNumber = testFunctionalityNumber;
	}

	public String getTestFunctionalityEdition() {
		return testFunctionalityEdition;
	}

	public void setTestFunctionalityEdition(String testFunctionalityEdition) {
		this.testFunctionalityEdition = testFunctionalityEdition;
	}

}
