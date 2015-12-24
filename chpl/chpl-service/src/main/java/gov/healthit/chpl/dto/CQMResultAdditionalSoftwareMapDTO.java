package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CQMResultAdditionalSoftwareMapEntity;

import java.util.Date;


public class CQMResultAdditionalSoftwareMapDTO {
	
	private Long id;
	private Long cqmResultId;
	private Long additionalSoftwareId;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	
	public CQMResultAdditionalSoftwareMapDTO(){}
	
	public CQMResultAdditionalSoftwareMapDTO(CQMResultAdditionalSoftwareMapEntity entity){
		
		this.id = entity.getId();
		this.additionalSoftwareId = entity.getAdditionalSoftwareId();
		this.cqmResultId = entity.getCQMResultId();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCqmResultId() {
		return cqmResultId;
	}
	public void setCqmResultId(Long cqmResultId) {
		this.cqmResultId = cqmResultId;
	}
	public Long getAdditionalSoftwareId() {
		return additionalSoftwareId;
	}
	public void setAdditionalSoftwareId(Long additionalSoftwareId) {
		this.additionalSoftwareId = additionalSoftwareId;
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
	
}
