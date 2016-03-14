package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.EducationTypeEntity;

public class TestParticipantDTO {

	private Long id;
	private String name;
	
	public TestParticipantDTO(){}
	
	public TestParticipantDTO(EducationTypeEntity entity)
	{
		if(entity != null) {
			this.id = entity.getId();
			this.name = entity.getName();
		}
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
