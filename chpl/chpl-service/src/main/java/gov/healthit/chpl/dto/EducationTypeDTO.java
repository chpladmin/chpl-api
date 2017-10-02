package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.EducationTypeEntity;

public class EducationTypeDTO implements Serializable {
	private static final long serialVersionUID = -5167706321750440799L;
	private Long id;
	private String name;

	public EducationTypeDTO() {}

	public EducationTypeDTO(EducationTypeEntity entity)
	{
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
