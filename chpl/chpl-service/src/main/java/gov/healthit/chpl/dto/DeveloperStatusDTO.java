package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.DeveloperStatusEntity;

public class DeveloperStatusDTO {

	private Long id;
	private String statusName;
	
	public DeveloperStatusDTO(){
	}

	public DeveloperStatusDTO(DeveloperStatusEntity entity) {
		this.id = entity.getId();
		this.statusName = entity.getName().toString();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
}
