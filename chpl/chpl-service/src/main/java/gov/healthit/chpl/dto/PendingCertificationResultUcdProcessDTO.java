package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultUcdProcessEntity;

public class PendingCertificationResultUcdProcessDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long ucdProcessId;
	private String ucdProcessName;
	private String ucdProcessDetails;
	
	public PendingCertificationResultUcdProcessDTO() {}
	
	public PendingCertificationResultUcdProcessDTO(PendingCertificationResultUcdProcessEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setUcdProcessName(entity.getUcdProcessName());
		this.setUcdProcessId(entity.getUcdProcessId());
		this.setUcdProcessDetails(entity.getUcdProcessDetails());
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

	public Long getUcdProcessId() {
		return ucdProcessId;
	}

	public void setUcdProcessId(Long ucdProcessId) {
		this.ucdProcessId = ucdProcessId;
	}

	public String getUcdProcessDetails() {
		return ucdProcessDetails;
	}

	public void setUcdProcessDetails(String ucdProcessDetails) {
		this.ucdProcessDetails = ucdProcessDetails;
	}

	public String getUcdProcessName() {
		return ucdProcessName;
	}

	public void setUcdProcessName(String ucdProcessName) {
		this.ucdProcessName = ucdProcessName;
	}
}
