package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestProcedureEntity;

public class PendingCertificationResultTestProcedureDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long testProcedureId;
	private String version;
	
	public PendingCertificationResultTestProcedureDTO() {}
	
	public PendingCertificationResultTestProcedureDTO(PendingCertificationResultTestProcedureEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setTestProcedureId(entity.getTestProcedureId());
		this.setVersion(entity.getTestProcedureVersion());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertificationResultId() {
		return pendingCertificationResultId;
	}

	public void setPendingCertificationResultId(Long pendingCertificationResultId) {
		this.pendingCertificationResultId = pendingCertificationResultId;
	}

	public Long getTestProcedureId() {
		return testProcedureId;
	}

	public void setTestProcedureId(Long testProcedureId) {
		this.testProcedureId = testProcedureId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
