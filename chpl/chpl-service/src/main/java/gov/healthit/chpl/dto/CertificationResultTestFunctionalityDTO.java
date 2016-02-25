package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultTestFunctionalityEntity;

public class CertificationResultTestFunctionalityDTO {
	private Long id;
	private Long certificationResultId;
	private Long testFunctionalityId;
	private String testFunctionalityName;
	private String testFunctionalityNumber;
	private Boolean deleted;
	
	public CertificationResultTestFunctionalityDTO(){}
	
	public CertificationResultTestFunctionalityDTO(CertificationResultTestFunctionalityEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testFunctionalityId = entity.getTestFunctionalityId();
		if(entity.getTestFunctionality() != null) {
			this.testFunctionalityName = entity.getTestFunctionality().getName();
			this.testFunctionalityNumber = entity.getTestFunctionality().getNumber();
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

}
