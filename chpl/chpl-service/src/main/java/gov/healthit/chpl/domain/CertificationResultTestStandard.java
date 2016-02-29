package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;

public class CertificationResultTestStandard {
	private Long id;
	private Long testStandardId;
	private String testStandardName;
	private String testStandardNumber;

	public CertificationResultTestStandard() {
		super();
	}
	
	public CertificationResultTestStandard(CertificationResultTestStandardDTO dto) {
		this.id = dto.getId();
		this.testStandardId = dto.getTestStandardId();
		this.testStandardName = dto.getTestStandardName();
		this.testStandardNumber = dto.getTestStandardNumber();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestStandardId() {
		return testStandardId;
	}

	public void setTestStandardId(Long testStandardId) {
		this.testStandardId = testStandardId;
	}

	public String getTestStandardName() {
		return testStandardName;
	}

	public void setTestStandardName(String testStandardName) {
		this.testStandardName = testStandardName;
	}

	public String getTestStandardNumber() {
		return testStandardNumber;
	}

	public void setTestStandardNumber(String testStandardNumber) {
		this.testStandardNumber = testStandardNumber;
	}

}
