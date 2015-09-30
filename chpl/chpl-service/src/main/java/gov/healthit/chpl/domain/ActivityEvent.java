package gov.healthit.chpl.domain;

import java.util.Date;

public class ActivityEvent {
	
	private Long id;
	private String description;
	private Date activityDate;
	private Long activityObjectId;
	private Long activityObjectClassId;
	private String activityClass;
	
	public ActivityEvent(){}
	
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
	public Long getActivityObjectClassId() {
		return activityObjectClassId;
	}
	public void setActivityObjectClassId(Long activityObjectClassId) {
		this.activityObjectClassId = activityObjectClassId;
	}
	public String getActivityClass() {
		return activityClass;
	}
	public void setActivityClass(String activityClass) {
		this.activityClass = activityClass;
	}
}
