package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.AdditionalSoftwareEntity;

import java.util.Date;

public class AdditionalSoftwareDTO {
	
	
	private Long id;
	private Long certifiedProductId;
	private Long certifiedProductSelfId;
	private Date creationDate;
	private Boolean deleted;
	private String justification;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String name;
	private String version;
	
	public AdditionalSoftwareDTO(){}
	
	public AdditionalSoftwareDTO(AdditionalSoftwareEntity entity){
		
		this.id = entity.getId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.certifiedProductSelfId = entity.getCertifiedProductSelfId();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.justification = entity.getJustification();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.name = entity.getName();
		this.version = entity.getVersion();
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
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
	public String getJustification() {
		return justification;
	}
	public void setJustification(String justification) {
		this.justification = justification;
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Long getCertifiedProductSelfId() {
		return certifiedProductSelfId;
	}
	public void setCertifiedProductSelfId(Long certifiedProductSelfId) {
		this.certifiedProductSelfId = certifiedProductSelfId;
	}
	
}
