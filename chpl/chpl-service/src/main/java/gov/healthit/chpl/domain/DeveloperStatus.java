package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.DeveloperStatusDTO;

public class DeveloperStatus {
	private Long id;
	private String status;
	
	public DeveloperStatus() {
	}
	
	public DeveloperStatus(DeveloperStatusDTO dto) {
		this.id = dto.getId();
		this.status = dto.getStatusName();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
