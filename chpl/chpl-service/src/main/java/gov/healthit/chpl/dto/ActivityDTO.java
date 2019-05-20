package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.util.Util;

public class ActivityDTO implements Serializable {
    private static final long serialVersionUID = -8364552955791049631L;
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

    public ActivityDTO() {
    }

    public ActivityDTO(ActivityEntity entity) {

        this.id = entity.getId();
        this.description = entity.getDescription();
        this.originalData = entity.getOriginalData();
        this.newData = entity.getNewData();
        this.activityDate = entity.getActivityDate();
        this.activityObjectId = entity.getActivityObjectId();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();

        if (entity.getConcept() != null) {
            this.concept = ActivityConcept.valueOf(entity.getConcept().getConcept());
        }
        if (entity.getUser() != null) {
            this.user = new UserDTO(entity.getUser());
        }
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

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public ActivityConcept getConcept() {
        return concept;
    }

    public void setConcept(final ActivityConcept concept) {
        this.concept = concept;
    }

    public String getOriginalData() {
        return originalData;
    }

    public void setOriginalData(final String originalData) {
        this.originalData = originalData;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(final String newData) {
        this.newData = newData;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(final UserDTO user) {
        this.user = user;
    }
}
