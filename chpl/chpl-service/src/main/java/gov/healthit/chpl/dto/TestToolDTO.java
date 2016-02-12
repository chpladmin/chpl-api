package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestToolEntity;

public class TestToolDTO {
	private Long id;
	private Long certificationEditionId;
	private String name;
	private String version;
	private String description;
	
	public TestToolDTO(){}
	
	public TestToolDTO(TestToolEntity entity){		
		this.id = entity.getId();
		this.certificationEditionId = entity.getCertificationEditionId();
		this.name = entity.getName();
		this.version = entity.getVersion();
		this.description = entity.getDescription();
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

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
