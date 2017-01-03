package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationResultTestToolDTO;

public class CertificationResultTestTool implements Serializable {
	private static final long serialVersionUID = 2785949879671019720L;
	private Long id;
	private Long testToolId;
	private String testToolName;
	private String testToolVersion;
	private boolean retired;
	
	public CertificationResultTestTool() {
		super();
	}
	
	public CertificationResultTestTool(CertificationResultTestToolDTO dto) {
		this.id = dto.getId();
		this.testToolId = dto.getTestToolId();
		this.testToolName = dto.getTestToolName();
		this.testToolVersion = dto.getTestToolVersion();
		this.retired = dto.isRetired();
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

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}
