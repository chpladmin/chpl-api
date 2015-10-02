package gov.healthit.chpl.domain;

import gov.healthit.chpl.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.Date;

public class ActivityEvent {
	
	private Long id;
	private String description;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	
	public ActivityEvent(){}
	public ActivityEvent(ActivityDTO dto){
		
		this.id = dto.getId();
		this.description = dto.getDescription();
		this.activityDate = dto.getActivityDate();
		this.activityObjectId = dto.getActivityObjectId();
		this.concept = dto.getConcept();
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
	public ActivityConcept getConcept() {
		return concept;
	}
	public void setConcept(ActivityConcept concept) {
		this.concept = concept;
	}
	
}
