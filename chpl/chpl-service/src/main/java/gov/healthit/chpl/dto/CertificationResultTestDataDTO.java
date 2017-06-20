package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestDataEntity;

public class CertificationResultTestDataDTO implements Serializable {
	private static final long serialVersionUID = -8409772564902652781L;
	private Long id;
	private Long certificationResultId;
	private String version;
	private String alteration;
	
	public CertificationResultTestDataDTO(){}
	
	public CertificationResultTestDataDTO(CertificationResultTestDataEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.version = entity.getTestDataVersion();
		this.alteration = entity.getAlterationDescription();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
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
