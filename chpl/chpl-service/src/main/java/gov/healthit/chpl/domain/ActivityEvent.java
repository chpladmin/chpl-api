package gov.healthit.chpl.domain;


import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.domain.concept.ActivityConcept;

public class ActivityEvent implements Serializable {
	private static final long serialVersionUID = -8220712127605295980L;
	private Long id;
	private String description;
	private JsonNode originalData;
	private JsonNode newData;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	private User responsibleUser;

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

	public User getResponsibleUser() {
		return responsibleUser;
	}

	public void setResponsibleUser(User responsibleUser) {
		this.responsibleUser = responsibleUser;
	}
}
