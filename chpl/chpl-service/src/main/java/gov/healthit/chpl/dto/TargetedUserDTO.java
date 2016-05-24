package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TargetedUserEntity;

public class TargetedUserDTO {
	private Long id;
	private String name;
	
	public TargetedUserDTO(){}
	
	public TargetedUserDTO(TargetedUserEntity entity){
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
