package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.ApiKeyActivityEntity;

import java.util.Date;

public class ApiKeyActivityDTO {
	
	private Long id;
	private Long apiKeyId;
	private String apiCallPath;
	private Date creationDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Boolean deleted;
	
	public ApiKeyActivityDTO(){}
	
	public ApiKeyActivityDTO(ApiKeyActivityEntity entity){
		
		this.id = entity.getId();
		this.apiKeyId = entity.getApiKeyId();
		this.apiCallPath = entity.getApiCallPath();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.deleted = entity.getDeleted();
		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getApiKeyId() {
		return apiKeyId;
	}
	public void setApiKeyId(Long apiKeyId) {
		this.apiKeyId = apiKeyId;
	}
	public String getApiCallPath() {
		return apiCallPath;
	}
	public void setApiCallPath(String apiCallPath) {
		this.apiCallPath = apiCallPath;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

}
