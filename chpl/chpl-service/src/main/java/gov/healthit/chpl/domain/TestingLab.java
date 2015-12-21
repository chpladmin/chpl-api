package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.TestingLabDTO;

public class TestingLab {
	
	private Long id;
	private String atlCode;
	private String name;
	private String website;
	private String accredidationNumber;
	private Address address;
	
	public TestingLab() {}
	
	public TestingLab(TestingLabDTO dto) {
		this.id = dto.getId();
		this.atlCode = dto.getTestingLabCode();
		this.name = dto.getName();
		this.website = dto.getWebsite();
		this.accredidationNumber = dto.getAccredidationNumber();
		if(dto.getAddress() != null) {
			this.address = new Address(dto.getAddress());
		}
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
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}

	public String getAtlCode() {
		return atlCode;
	}

	public void setAtlCode(String atlCode) {
		this.atlCode = atlCode;
	}

	public String getAccredidationNumber() {
		return accredidationNumber;
	}

	public void setAccredidationNumber(String accredidationNumber) {
		this.accredidationNumber = accredidationNumber;
	}
}
