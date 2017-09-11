package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CertificationCriterionEntity;

public class CertificationCriterionDTO implements Serializable {
	private static final long serialVersionUID = -1129602624256345286L;
	private Long id;
	private Boolean automatedMeasureCapable;
	private Boolean automatedNumeratorCapable;
	private Long certificationEditionId;
	private String certificationEdition;
	private Date creationDate;
	private Boolean deleted;
	private String description;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String number;
	private Boolean requiresSed;
	private String title;
	
	public CertificationCriterionDTO(){}
	public CertificationCriterionDTO(CertificationCriterionEntity entity){
		
		this.id = entity.getId();
		this.automatedMeasureCapable = entity.isAutomatedMeasureCapable();
		this.automatedNumeratorCapable = entity.isAutomatedNumeratorCapable();
		this.certificationEditionId = entity.getCertificationEditionId();
		if(entity.getCertificationEdition() != null) {
			this.certificationEdition = entity.getCertificationEdition().getYear();
		}
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.isDeleted();
		this.description = entity.getDescription();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.number = entity.getNumber();
		
		this.requiresSed = entity.isRequiresSed();
		this.title = entity.getTitle();
		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getAutomatedMeasureCapable() {
		return automatedMeasureCapable;
	}
	public void setAutomatedMeasureCapable(Boolean automatedMeasureCapable) {
		this.automatedMeasureCapable = automatedMeasureCapable;
	}
	public Boolean getAutomatedNumeratorCapable() {
		return automatedNumeratorCapable;
	}
	public void setAutomatedNumeratorCapable(Boolean automatedNumeratorCapable) {
		this.automatedNumeratorCapable = automatedNumeratorCapable;
	}
	public Long getCertificationEditionId() {
		return certificationEditionId;
	}
	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
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
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public Boolean getRequiresSed() {
		return requiresSed;
	}
	public void setRequiresSed(Boolean requiresSed) {
		this.requiresSed = requiresSed;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
}
