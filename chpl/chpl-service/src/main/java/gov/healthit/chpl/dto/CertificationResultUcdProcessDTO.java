package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultUcdProcessEntity;

public class CertificationResultUcdProcessDTO {
	private Long id;
	private Long certificationResultId;
	private Long ucdProcessId;
	private String ucdProcessName;
	private String ucdProcessDetails;
	
	public CertificationResultUcdProcessDTO(){}
	
	public CertificationResultUcdProcessDTO(CertificationResultUcdProcessEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.ucdProcessId = entity.getUcdProcessId();
		this.ucdProcessDetails = entity.getUcdProcessDetails();
		if(entity.getUcdProcess() != null) {
			this.ucdProcessName = entity.getUcdProcess().getName();
		}
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

	public Long getUcdProcessId() {
		return ucdProcessId;
	}

	public void setUcdProcessId(Long ucdProcessId) {
		this.ucdProcessId = ucdProcessId;
	}

	public String getUcdProcessName() {
		return ucdProcessName;
	}

	public void setUcdProcessName(String ucdProcessName) {
		this.ucdProcessName = ucdProcessName;
	}

	public String getUcdProcessDetails() {
		return ucdProcessDetails;
	}

	public void setUcdProcessDetails(String ucdProcessDetails) {
		this.ucdProcessDetails = ucdProcessDetails;
	}
}
