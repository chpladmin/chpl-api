package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestStandardEntity;

public class TestStandardDTO implements Serializable {
	private static final long serialVersionUID = -7473233688407477963L;
	private Long id;
	private String description;
	private String name;
	private Long certificationEditionId;
	private String year;
	
	public TestStandardDTO(){}
	
	public TestStandardDTO(TestStandardEntity entity){		
		this.id = entity.getId();
		this.description = entity.getDescription();
		this.name = entity.getName();
		this.certificationEditionId = entity.getCertificationEditionId();
		if(entity.getCertificationEdition() != null) {
			this.year = entity.getCertificationEdition().getYear();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String name) {
		this.description = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String number) {
		this.name = number;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}
}
