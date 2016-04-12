package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;

public class CertificationResultTestFunctionality {
	private Long id;
	private Long testFunctionalityId;
	private String description;
	private String name;

	public CertificationResultTestFunctionality() {
		super();
	}
	
	public CertificationResultTestFunctionality(CertificationResultTestFunctionalityDTO dto) {
		this.id = dto.getId();
		this.testFunctionalityId = dto.getTestFunctionalityId();
		this.description = dto.getTestFunctionalityName();
		this.name = dto.getTestFunctionalityNumber();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestFunctionalityId() {
		return testFunctionalityId;
	}

	public void setTestFunctionalityId(Long testFunctionalityId) {
		this.testFunctionalityId = testFunctionalityId;
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

	public void setName(String name) {
		this.name = name;
	}

}
