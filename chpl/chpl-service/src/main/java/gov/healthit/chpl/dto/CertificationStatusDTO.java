package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationStatusEntity;

public class CertificationStatusDTO {
	private Long id;
	private String status;
	
	public CertificationStatusDTO() {}
	public CertificationStatusDTO(CertificationStatusEntity entity) {
		this.setId(entity.getId());
		this.setStatus(entity.getStatus());
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
