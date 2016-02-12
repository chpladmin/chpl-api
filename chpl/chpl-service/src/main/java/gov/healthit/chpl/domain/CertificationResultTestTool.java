package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestToolDTO;

public class CertificationResultTestTool {
	private Long id;
	private Long testToolId;
	private String testToolName;
	private String testToolVersion;

	public CertificationResultTestTool() {
		super();
	}
	
	public CertificationResultTestTool(CertificationResultTestToolDTO dto) {
		this.id = dto.getId();
		this.testToolId = dto.getTestToolId();
		this.testToolName = dto.getTestToolName();
		this.testToolVersion = dto.getTestToolVersion();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
