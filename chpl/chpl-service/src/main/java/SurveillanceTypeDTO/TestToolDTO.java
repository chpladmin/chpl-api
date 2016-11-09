package SurveillanceTypeDTO;

import gov.healthit.chpl.entity.TestToolEntity;

public class TestToolDTO {
	private Long id;
	private String name;
	private String description;
	private boolean retired;
	
	public TestToolDTO(){}
	
	public TestToolDTO(TestToolEntity entity){		
		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.retired = entity.getRetired();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}
