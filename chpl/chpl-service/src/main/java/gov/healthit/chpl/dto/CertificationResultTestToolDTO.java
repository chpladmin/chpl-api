package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultTestToolEntity;

public class CertificationResultTestToolDTO {
	private Long id;
	private Long certificationResultId;
	private Long testToolId;
	private String testToolName;
	private String testToolVersion;
	private Boolean deleted;
	
	public CertificationResultTestToolDTO(){}
	
	public CertificationResultTestToolDTO(CertificationResultTestToolEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testToolId = entity.getTestToolId();
		if(entity.getTestTool() != null) {
			this.testToolName = entity.getTestTool().getName();
			this.testToolVersion = entity.getTestTool().getVersion();
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

	public Long getTestToolId() {
		return testToolId;
	}

	public void setTestToolId(Long testToolId) {
		this.testToolId = testToolId;
	}

	public String getTestToolName() {
		return testToolName;
	}

	public void setTestToolName(String testToolName) {
		this.testToolName = testToolName;
	}

	public String getTestToolVersion() {
		return testToolVersion;
	}

	public void setTestToolVersion(String testToolVersion) {
		this.testToolVersion = testToolVersion;
	}

}
