package gov.healthit.chpl.domain.activity;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.util.Util;

/**
 * Summary data about an activity event. This is intended to be a superclass
 * containing fields that could be applicable to activity on any type of object.
 * @author kekey
 *
 */
public class ActivityMetadata implements Serializable {
    private static final long serialVersionUID = -3855142961571082535L;

    private Long id;
    private String description;
    private Date activityDate;
    private Long activityObjectId;
    private ActivityConcept concept;
    private User responsibleUser;

    public ActivityMetadata() {
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

    public User getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(final User responsibleUser) {
        this.responsibleUser = responsibleUser;
    }
}
