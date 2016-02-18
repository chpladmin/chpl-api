package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultAdditionalSoftwareEntity;

public class PendingCertificationResultAdditionalSoftwareDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long certifiedProductId;
	private String chplId;
	private String name;
	private String version;
	private String justification;
	
	public PendingCertificationResultAdditionalSoftwareDTO() {}
	
	public PendingCertificationResultAdditionalSoftwareDTO(PendingCertificationResultAdditionalSoftwareEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setCertifiedProductId(entity.getCertifiedProductId());
		this.setChplId(entity.getChplId());
		this.setName(entity.getSoftwareName());
		this.setVersion(entity.getSoftwareVersion());
		this.setJustification(entity.getJustification());
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

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public String getChplId() {
		return chplId;
	}

	public void setChplId(String chplId) {
		this.chplId = chplId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}
}
