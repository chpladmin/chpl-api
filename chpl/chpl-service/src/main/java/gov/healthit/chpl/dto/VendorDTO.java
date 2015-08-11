package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.VendorEntity;

import java.util.Date;

public class VendorDTO {

	private Long id;
	private Long addressId;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String name;
	private String website;
	
	public VendorDTO(){}
	
	public VendorDTO(VendorEntity entity){
		
		this.id = entity.getId();
		this.addressId = entity.getAddressId();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.isDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.name = entity.getName();
		this.website = entity.getWebsite();
		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAddressId() {
		return addressId;
	}
	public void setAddressId(Long addressId) {
		this.addressId = addressId;
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
	
}
