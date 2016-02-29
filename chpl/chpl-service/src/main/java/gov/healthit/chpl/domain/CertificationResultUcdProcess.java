package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;

public class CertificationResultUcdProcess {
	private Long id;
	private Long ucdProcessId;
	private String ucdProcessName;
	private String ucdProcessDetails;

	public CertificationResultUcdProcess() {
		super();
	}
	
	public CertificationResultUcdProcess(CertificationResultUcdProcessDTO dto) {
		this.id = dto.getId();
		this.ucdProcessId = dto.getUcdProcessId();
		this.ucdProcessName = dto.getUcdProcessName();
		this.ucdProcessDetails = dto.getUcdProcessDetails();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
