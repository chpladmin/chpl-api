package gov.healthit.chpl.questionableactivity.entity;

import java.util.Date;

import gov.healthit.chpl.entity.ActivityEntity;

public interface QuestionableActivityBaseEntity {

    Long getId();
    void setId(Long id);
    Long getTriggerId();
    void setTriggerId(Long triggerId);
    ActivityEntity getActivity();
    void setActivity(ActivityEntity activity);
    QuestionableActivityTriggerEntity getTrigger();
    void setTrigger(QuestionableActivityTriggerEntity trigger);
    String getBefore();
    void setBefore(String before);
    String getAfter();
    void setAfter(String after);
    Date getActivityDate();
    void setActivityDate(Date activityDate);
    Boolean getDeleted();
    void setDeleted(Boolean deleted);
    Long getLastModifiedUser();
    void setLastModifiedUser(Long lastModifiedUser);
    Date getCreationDate();
    void setCreationDate(Date creationDate);
    Date getLastModifiedDate();
    void setLastModifiedDate(Date lastModifiedDate);
}

