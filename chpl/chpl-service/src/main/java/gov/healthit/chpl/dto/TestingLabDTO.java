package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestingLabEntity;

import java.util.Date;

public class TestingLabDTO {

	private String testingLabCode;
	private Long id;
	private AddressDTO address;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String name;
	private String website;
	private String accredidationNumber;
	
	public TestingLabDTO(){}
	
	public TestingLabDTO(TestingLabEntity entity){
		this.id = entity.getId();
		this.testingLabCode = entity.getTestingLabCode();
		if(entity.getAddress() != null) {
			this.address = new AddressDTO(entity.getAddress());			
		}
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.isDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.name = entity.getName();
		this.website = entity.getWebsite();
		this.accredidationNumber = entity.getAccredidationNumber();
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
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

	public String getTestingLabCode() {
		return testingLabCode;
	}

	public void setTestingLabCode(String testingLabCode) {
		this.testingLabCode = testingLabCode;
	}

	public String getAccredidationNumber() {
		return accredidationNumber;
	}

	public void setAccredidationNumber(String accredidationNumber) {
		this.accredidationNumber = accredidationNumber;
	}
}
