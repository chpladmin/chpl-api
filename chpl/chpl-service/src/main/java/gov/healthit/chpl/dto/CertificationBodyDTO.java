package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.util.Date;

public class CertificationBodyDTO {
	
	private String acbCode;
	private Date creationDate;
	private Boolean deleted;
	private Long id;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String name;
	private String website;
	private AddressDTO address;
	
	public CertificationBodyDTO(){}
	
	public CertificationBodyDTO(CertificationBodyEntity entity){
		this.id = entity.getId();
		this.acbCode = entity.getAcbCode();
		this.deleted = entity.getDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.name = entity.getName();
		this.website = entity.getWebsite();
		this.creationDate = entity.getCreationDate();
		if(entity.getAddress() != null) {
			this.address = new AddressDTO(entity.getAddress());			
		}
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public String getAcbCode() {
		return acbCode;
	}

	public void setAcbCode(String acbCode) {
		this.acbCode = acbCode;
	}
	
}