package gov.healthit.chpl.domain;


import gov.healthit.chpl.domain.ActivityConcept;
import java.util.Date;

public class ActivityEventSummary {
	
	private Long id;
	private String description;
	private Date activityDate;
	private Long activityObjectId;
	private ActivityConcept concept;
	
	public ActivityEventSummary(){}
	
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
