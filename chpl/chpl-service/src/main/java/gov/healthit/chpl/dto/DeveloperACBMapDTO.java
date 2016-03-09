package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.DeveloperACBMapEntity;

public class DeveloperACBMapDTO {
	private Long id;
	private Long developerId;
	private Long acbId;
	private String transparencyAttestation;
	
	public DeveloperACBMapDTO(){}
	
	public DeveloperACBMapDTO(DeveloperACBMapEntity entity){
		this.id = entity.getId();
		this.developerId = entity.getDeveloperId();
		this.acbId = entity.getCertificationBodyId();
		if(entity.getTransparencyAttestation() != null) {
			this.transparencyAttestation = entity.getTransparencyAttestation().toString();
		}
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

	public String getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(String transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
}
