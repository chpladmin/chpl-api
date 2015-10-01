package gov.healthit.chpl.dto;

import gov.healthit.chpl.activity.ActivityConcept;
import gov.healthit.chpl.entity.ActivityEntity;

import java.util.Date;

public class ActivityDTO {

	private Long id;
	private String description;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	private Date creationDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Long deleted;
	
	public ActivityDTO(){}
	
	public ActivityDTO(ActivityEntity entity){
		
		this.id = entity.getId();
		this.description = entity.getDescription();
		this.activityDate = entity.getActivityDate();
		this.activityObjectId = entity.getActivityObjectId();
		this.concept = entity.getConcept();
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	public Long getActivityObjectId() {
		return activityObjectId;
	}
	public void setActivityObjectId(Long activityObjectId) {
		this.activityObjectId = activityObjectId;
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
	public Long getDeleted() {
		return deleted;
	}
	public void setDeleted(Long deleted) {
		this.deleted = deleted;
	}
	public ActivityConcept getConcept() {
		return concept;
	}
	public void setConcept(ActivityConcept concept) {
		this.concept = concept;
	}
}
