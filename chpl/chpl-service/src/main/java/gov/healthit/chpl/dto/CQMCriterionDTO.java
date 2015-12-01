package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CQMCriterionEntity;

import java.util.Date;

public class CQMCriterionDTO {

	private String cmsId;
	private Long cqmCriterionTypeId;
	private String cqmDomain;
	private Long cqmVersionId;
	private String cqmVersion;
	private Date creationDate;
	private Boolean deleted;
	private String description;
	private Long id;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String nqfNumber;
	private String number;
	private String title;
	private Boolean retired;
	
	public CQMCriterionDTO(){}
	
	public CQMCriterionDTO(CQMCriterionEntity entity){
		
		this.cmsId = entity.getCmsId();
		this.cqmCriterionTypeId = entity.getCqmCriterionTypeId();
		this.cqmDomain = entity.getCqmDomain();
		this.cqmVersionId = entity.getCqmVersionId();
		this.cqmVersion = entity.getCqmVersion();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.description = entity.getDescription();
		this.id = entity.getId();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.nqfNumber = entity.getNqfNumber();
		this.number = entity.getNumber();
		this.title = entity.getTitle();
		this.setRetired(entity.getRetired());
		
	}
	
	
	public String getCmsId() {
		return cmsId;
	}
	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
	public Long getCqmCriterionTypeId() {
		return cqmCriterionTypeId;
	}
	public void setCqmCriterionTypeId(Long cqmCriterionTypeId) {
		this.cqmCriterionTypeId = cqmCriterionTypeId;
	}
	public String getCqmDomain() {
		return cqmDomain;
	}
	public void setCqmDomain(String cqmDomain) {
		this.cqmDomain = cqmDomain;
	}
	public Long getCqmVersionId() {
		return cqmVersionId;
	}
	public void setCqmVersionId(Long cqmVersion) {
		this.cqmVersionId = cqmVersion;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getNqfNumber() {
		return nqfNumber;
	}
	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCqmVersion() {
		return cqmVersion;
	}
	public void setCqmVersion(String cqmVersion) {
		this.cqmVersion = cqmVersion;
	}
	public Boolean getRetired() {
		return retired;
	}
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
}
