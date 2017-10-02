package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.MacraMeasureEntity;

public class MacraMeasureDTO implements Serializable {
	private static final long serialVersionUID = -1863384989196377585L;
	private Long id;
	private Long criteriaId;
	private CertificationCriterionDTO criteria;
	private String value;
	private String name;
	private String description;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;

	public MacraMeasureDTO(){
	}
	public MacraMeasureDTO(MacraMeasureEntity entity){
		this();

		this.id = entity.getId();
		this.criteriaId = entity.getCertificationCriterionId();
		if(entity.getCertificationCriterion() != null) {
			this.criteria = new CertificationCriterionDTO(entity.getCertificationCriterion());
		}
		this.value = entity.getValue();
		this.name = entity.getName();
		this.description = entity.getDescription();

		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
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
	public Long getCriteriaId() {
		return criteriaId;
	}
	public void setCriteriaId(Long criteriaId) {
		this.criteriaId = criteriaId;
	}
	public CertificationCriterionDTO getCriteria() {
		return criteria;
	}
	public void setCriteria(CertificationCriterionDTO criteria) {
		this.criteria = criteria;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
