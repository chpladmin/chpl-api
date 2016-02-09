package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultAdditionalSoftwareMapEntity;

import java.util.Date;

public class CertificationResultAdditionalSoftwareMapDTO {
	
	
	private Long id;
	private Long additionalSoftwareId;
	private Long certificationResultId;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	
	public CertificationResultAdditionalSoftwareMapDTO(){}
	
	public CertificationResultAdditionalSoftwareMapDTO(CertificationResultAdditionalSoftwareMapEntity entity){
		
		this.id = entity.getId();
		this.additionalSoftwareId = entity.getAdditionalSoftwareId();
		this.certificationResultId = entity.getCertificationResultId();
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
	public Long getCertificationResultId() {
		return certificationResultId;
	}
	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
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
