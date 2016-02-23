package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestStandardEntity;

public class TestStandardDTO {
	private Long id;
	private String name;
	private String number;
	
	public TestStandardDTO(){}
	
	public TestStandardDTO(TestStandardEntity entity){		
		this.id = entity.getId();
		this.name = entity.getName();
		this.number = entity.getNumber();
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
