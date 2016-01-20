package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.AdditionalSoftwareDTO;

public class AdditionalSoftware {

	private Long additionalSoftwareId;
	private Long certifiedProductId;
	private Long certifiedProductSelf;
	private String justification;
	private String name;
	private String version;
	
	public AdditionalSoftware() {}
	public AdditionalSoftware(AdditionalSoftwareDTO additionalSoftwareDTO) {
		this.setAdditionalSoftwareId(additionalSoftwareDTO.getId());
		this.setCertifiedProductId(additionalSoftwareDTO.getCertifiedProductId());
		this.setCertifiedProductSelf(additionalSoftwareDTO.getCertifiedProductSelfId());
		this.setJustification(additionalSoftwareDTO.getJustification());
		this.setName(additionalSoftwareDTO.getName());
		this.setVersion(additionalSoftwareDTO.getVersion());
	}
	
	public Long getAdditionalSoftwareId() {
		return additionalSoftwareId;
	}
	public void setAdditionalSoftwareId(Long additionalSoftwareid) {
		this.additionalSoftwareId = additionalSoftwareid;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}
	public String getJustification() {
		return justification;
	}
	public void setJustification(String justification) {
		this.justification = justification;
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
	public Long getCertifiedProductSelf() {
		return certifiedProductSelf;
	}
	public void setCertifiedProductSelf(Long certifiedProductSelf) {
		this.certifiedProductSelf = certifiedProductSelf;
	}
}
