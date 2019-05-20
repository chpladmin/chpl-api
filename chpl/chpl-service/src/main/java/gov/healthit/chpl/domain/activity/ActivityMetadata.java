package gov.healthit.chpl.domain.activity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.domain.auth.User;
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
    private ActivityConcept concept;
    private Set<ActivityCategory> categories = new HashSet<ActivityCategory>();
    private Date date;
    private Long objectId;
    private User responsibleUser;
    private String description;

    public ActivityMetadata() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getDate() {
        return Util.getNewDate(date);
    }

    public void setDate(final Date date) {
        this.date = Util.getNewDate(date);
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(final Long objectId) {
        this.objectId = objectId;
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

    public Set<ActivityCategory> getCategories() {
        return categories;
    }

    public void setCategories(final Set<ActivityCategory> categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
