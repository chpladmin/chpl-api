package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.util.Date;

public class CertificationBodyDTO {
	
	
	private Date creationDate;
	private Boolean deleted;
	private Long id;
	private Date lastModifiedDate;
	private Integer lastModifiedUser;
	private String name;
	private String website;
	
	
	public CertificationBodyDTO(){}
	
	public CertificationBodyDTO(CertificationBodyEntity entity){
		this.creationDate = entity.getCreationDate();
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
	public Integer getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Integer lastModifiedUser) {
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