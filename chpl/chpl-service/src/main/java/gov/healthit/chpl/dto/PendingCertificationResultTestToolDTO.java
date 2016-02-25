package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestToolEntity;

public class PendingCertificationResultTestToolDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long testToolId;
	private String name;
	private String version;
	
	public PendingCertificationResultTestToolDTO() {}
	
	public PendingCertificationResultTestToolDTO(PendingCertificationResultTestToolEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setTestToolId(entity.getTestToolId());
		this.setName(entity.getTestToolName());
		this.setVersion(entity.getTestToolVersion());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getTestToolId() {
		return testToolId;
	}

	public void setTestToolId(Long testToolId) {
		this.testToolId = testToolId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
