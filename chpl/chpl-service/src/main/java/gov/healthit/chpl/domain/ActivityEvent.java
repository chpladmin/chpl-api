package gov.healthit.chpl.domain;



import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.domain.ActivityConcept;

import java.util.Date;

public class ActivityEvent {
	
	private Long id;
	private String description;
	private String originalData;
	private String newData;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	
	public ActivityEvent(){}
	public ActivityEvent(ActivityDTO dto){
		
		this.id = dto.getId();
		this.description = dto.getDescription();
		this.originalData = dto.getOriginalData();
		this.newData = dto.getNewData();
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
	
}
