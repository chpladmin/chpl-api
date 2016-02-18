package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultTestProcedureEntity;

public class CertificationResultTestProcedureDTO {
	private Long id;
	private Long certificationResultId;
	private Long testProcedureId;
	private String testProcedureVersion;
	private Boolean deleted;
	
	public CertificationResultTestProcedureDTO(){}
	
	public CertificationResultTestProcedureDTO(CertificationResultTestProcedureEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testProcedureId = entity.getTestProcedureId();
		if(entity.getTestProcedure() != null) {
			this.testProcedureVersion = entity.getTestProcedure().getVersion();	
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

	public Long getTestProcedureId() {
		return testProcedureId;
	}

	public void setTestProcedureId(Long testProcedureId) {
		this.testProcedureId = testProcedureId;
	}

	public String getTestProcedureVersion() {
		return testProcedureVersion;
	}

	public void setTestProcedureVersion(String testProcedureVersion) {
		this.testProcedureVersion = testProcedureVersion;
	}

}
