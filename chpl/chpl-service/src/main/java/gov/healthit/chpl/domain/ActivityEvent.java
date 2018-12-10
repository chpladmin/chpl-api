package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.util.Util;

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

    public ActivityEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Date getActivityDate() {
        return Util.getNewDate(activityDate);
    }

    public void setActivityDate(final Date activityDate) {
        this.activityDate = Util.getNewDate(activityDate);
    }

    public Long getActivityObjectId() {
        return activityObjectId;
    }

    public void setActivityObjectId(final Long activityObjectId) {
        this.activityObjectId = activityObjectId;
    }

    public ActivityConcept getConcept() {
        return concept;
    }

    public void setConcept(final ActivityConcept concept) {
        this.concept = concept;
    }

    public JsonNode getOriginalData() {
        return originalData;
    }

    public void setOriginalData(final JsonNode originalData) {
        this.originalData = originalData;
    }

    public JsonNode getNewData() {
        return newData;
    }

    public void setNewData(final JsonNode newData) {
        this.newData = newData;
    }

    public User getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(final User responsibleUser) {
        this.responsibleUser = responsibleUser;
    }
}
