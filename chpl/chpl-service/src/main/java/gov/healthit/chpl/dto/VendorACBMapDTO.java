package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.VendorACBMapEntity;

public class VendorACBMapDTO {
	private Long id;
	private Long vendorId;
	private Long acbId;
	private Boolean transparencyAttestation;
	
	public VendorACBMapDTO(){}
	
	public VendorACBMapDTO(VendorACBMapEntity entity){
		this.id = entity.getId();
		this.vendorId = entity.getVendorId();
		this.acbId = entity.getCertificationBodyId();
		this.transparencyAttestation = entity.getTransparencyAttestation();
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
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
