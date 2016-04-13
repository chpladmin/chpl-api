package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationIdEntity;

import java.util.Date;


public class CertificationIdDTO {
	
	private Long id;
	private String certificationId;
	private Long attestationYearId;
	private Long practiceTypeId;
	
	
	private Date creationDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	public CertificationIdDTO(){}
	public CertificationIdDTO(CertificationIdEntity entity){
		
		this.id = entity.getId();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.certificationId = entity.getCertificationId();
		this.attestationYearId = entity.getAttestationYearId();
		this.practiceTypeId = entity.getPracticeTypeId();

	}
	
	
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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

	public void setCertificationId(String certId) {
		this.certificationId = certId;
	}
	
	public String getCertificationId() {
		return this.certificationId;
	}
	
	public void setAttestationYearId(Long attestationYearId) {
		this.attestationYearId = attestationYearId;
	}

	public Long getAttestationYearId() {
		return this.attestationYearId;
	}
	
	public void setPracticeTypeId(Long practiceTypeId) {
		this.practiceTypeId = practiceTypeId;
	}

	public Long getPracticeTypeId() {
		return this.practiceTypeId;
	}
	
}
