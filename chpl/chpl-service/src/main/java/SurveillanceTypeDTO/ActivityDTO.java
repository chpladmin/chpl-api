package SurveillanceTypeDTO;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.entity.ActivityEntity;

import java.util.Date;


public class ActivityDTO {

	private Long id;
	private String description;
	private String originalData;
	private String newData;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	private Date creationDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Boolean deleted;
	private UserDTO user;
	
	public ActivityDTO(){}
	
	public ActivityDTO(ActivityEntity entity){
		
		this.id = entity.getId();
		this.description = entity.getDescription();
		this.originalData = entity.getOriginalData();
		this.newData = entity.getNewData();
		this.activityDate = entity.getActivityDate();
		this.activityObjectId = entity.getActivityObjectId();
		this.concept = entity.getConcept();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.deleted = entity.getDeleted();
		
		if(entity.getUser() != null) {
			this.user = new UserDTO(entity.getUser());
		}
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
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public ActivityConcept getConcept() {
		return concept;
	}
	public void setConcept(ActivityConcept concept) {
		this.concept = concept;
	}

	public String getOriginalData() {
		return originalData;
	}

	public void setOriginalData(String originalData) {
		this.originalData = originalData;
	}

	public String getNewData() {
		return newData;
	}

	public void setNewData(String newData) {
		this.newData = newData;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}
}
