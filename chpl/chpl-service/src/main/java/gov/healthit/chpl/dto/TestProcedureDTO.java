package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestProcedureEntity;

public class TestProcedureDTO {
	private Long id;
	private String version;
	
	public TestProcedureDTO(){}
	
	public TestProcedureDTO(TestProcedureEntity entity){		
		this.id = entity.getId();
		this.version = entity.getVersion();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
