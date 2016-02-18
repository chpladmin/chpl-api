package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;

public class CertificationResultTestProcedure {
	private Long id;
	private Long testProcedureId;
	private String testProcedureVersion;

	public CertificationResultTestProcedure() {
		super();
	}
	
	public CertificationResultTestProcedure(CertificationResultTestProcedureDTO dto) {
		this.id = dto.getId();
		this.testProcedureId = dto.getTestProcedureId();
		this.testProcedureVersion = dto.getTestProcedureVersion();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
