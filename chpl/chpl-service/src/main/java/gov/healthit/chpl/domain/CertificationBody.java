package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationBodyDTO;

public class CertificationBody {
	
	private Long id;
	private String name;
	private String website;
	private Address address;
	
	public CertificationBody() {}
	
	public CertificationBody(CertificationBodyDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.website = dto.getWebsite();
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
	
}
