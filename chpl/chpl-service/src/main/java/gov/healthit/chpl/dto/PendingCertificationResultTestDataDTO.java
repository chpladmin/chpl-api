package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestDataEntity;

public class PendingCertificationResultTestDataDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private String version;
	private String alteration;
	
	public PendingCertificationResultTestDataDTO() {}
	
	public PendingCertificationResultTestDataDTO(PendingCertificationResultTestDataEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setVersion(entity.getVersion());
		this.setAlteration(entity.getAlteration());
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAlteration() {
		return alteration;
	}

	public void setAlteration(String alteration) {
		this.alteration = alteration;
	}

}
