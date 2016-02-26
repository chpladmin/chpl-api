package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;

public class CertificationResultTestFunctionality {
	private Long id;
	private Long testFunctionalityId;
	private String name;
	private String number;

	public CertificationResultTestFunctionality() {
		super();
	}
	
	public CertificationResultTestFunctionality(CertificationResultTestFunctionalityDTO dto) {
		this.id = dto.getId();
		this.testFunctionalityId = dto.getTestFunctionalityId();
		this.name = dto.getTestFunctionalityName();
		this.number = dto.getTestFunctionalityNumber();
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
