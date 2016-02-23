package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultTestStandardEntity;

public class CertificationResultTestStandardDTO {
	private Long id;
	private Long certificationResultId;
	private Long testStandardId;
	private String testStandardName;
	private String testStandardNumber;
	private Boolean deleted;
	
	public CertificationResultTestStandardDTO(){}
	
	public CertificationResultTestStandardDTO(CertificationResultTestStandardEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testStandardId = entity.getTestStandardId();
		if(entity.getTestStandard() != null) {
			this.testStandardName = entity.getTestStandard().getName();
			this.testStandardNumber = entity.getTestStandard().getNumber();
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

	public Long getTestStandardId() {
		return testStandardId;
	}

	public void setTestStandardId(Long testStandardId) {
		this.testStandardId = testStandardId;
	}

	public String getTestStandardName() {
		return testStandardName;
	}

	public void setTestStandardName(String testStandardName) {
		this.testStandardName = testStandardName;
	}

	public String getTestStandardNumber() {
		return testStandardNumber;
	}

	public void setTestStandardNumber(String testStandardNumber) {
		this.testStandardNumber = testStandardNumber;
	}
}
