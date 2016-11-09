package SurveillanceTypeDTO;

import gov.healthit.chpl.entity.AgeRangeEntity;

public class AgeRangeDTO {

	private Long id;
	private String age;
	
	public AgeRangeDTO(){}
	
	public AgeRangeDTO(AgeRangeEntity entity)
	{
		this.id = entity.getId();
		this.age = entity.getAge();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
}
