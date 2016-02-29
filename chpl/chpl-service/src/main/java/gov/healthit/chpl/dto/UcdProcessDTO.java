package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestToolEntity;
import gov.healthit.chpl.entity.UcdProcessEntity;

public class UcdProcessDTO {
	private Long id;
	private String name;
	
	public UcdProcessDTO(){}
	
	public UcdProcessDTO(UcdProcessEntity entity){		
		this.id = entity.getId();
		this.name = entity.getName();
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
}
