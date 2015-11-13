package gov.healthit.chpl.domain;



import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.domain.ActivityConcept;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

public class ActivityEvent {
	
	private Long id;
	private String description;
	private JsonNode originalData;
	private JsonNode newData;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	
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
	public ActivityConcept getConcept() {
		return concept;
	}
	public void setConcept(ActivityConcept concept) {
		this.concept = concept;
	}
	public JsonNode getOriginalData() {
		return originalData;
	}
	public void setOriginalData(JsonNode originalData) {
		this.originalData = originalData;
	}
	public JsonNode getNewData() {
		return newData;
	}
	public void setNewData(JsonNode newData) {
		this.newData = newData;
	}
}
