package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.DeveloperACBMapEntity;

public class DeveloperACBMapDTO {
	private Long id;
	private Long developerId;
	private Long acbId;
	private Boolean transparencyAttestation;
	
	public DeveloperACBMapDTO(){}
	
	public DeveloperACBMapDTO(DeveloperACBMapEntity entity){
		this.id = entity.getId();
		this.developerId = entity.getDeveloperId();
		this.acbId = entity.getCertificationBodyId();
		this.transparencyAttestation = entity.getTransparencyAttestation();
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public Long getAcbId() {
		return acbId;
	}

	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}

	public Boolean getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(Boolean transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
}
