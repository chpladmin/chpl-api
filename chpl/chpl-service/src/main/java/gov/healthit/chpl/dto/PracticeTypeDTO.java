package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.PracticeTypeEntity;

public class PracticeTypeDTO implements Serializable {
	private static final long serialVersionUID = 6074108187522287741L;
	private Long id;
	private Date creationDate;
	private Boolean deleted;
	private String description;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String name;

	public PracticeTypeDTO(){}
	
	public PracticeTypeDTO(PracticeTypeEntity entity){
		
		this.id = entity.getId();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.isDeleted();
		this.description = entity.getDescription();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.name = entity.getName();
		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
