package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;

public class CertificationResultAdditionalSoftware {
	private Long id;
	private String name;
	private String version;
	private Long certifiedProductId;
	private String certifiedProductNumber;
	private String justification;
	private Long certificationResultId;

	public CertificationResultAdditionalSoftware() {
		super();
	}
	
	public CertificationResultAdditionalSoftware(CertificationResultAdditionalSoftwareDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.version = dto.getVersion();
		this.certifiedProductId = dto.getCertifiedProductId();
		this.justification = dto.getJustification();
		this.certificationResultId = dto.getCertificationResultId();
	}
	
	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}

	public String getCertifiedProductNumber() {
		return certifiedProductNumber;
	}

	public void setCertifiedProductNumber(String certifiedProductNumber) {
		this.certifiedProductNumber = certifiedProductNumber;
	}

}
