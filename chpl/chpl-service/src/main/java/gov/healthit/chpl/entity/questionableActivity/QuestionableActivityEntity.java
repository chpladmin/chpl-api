package gov.healthit.chpl.entity.questionableActivity;

import java.util.Date;

import gov.healthit.chpl.auth.entity.UserEntity;

public interface QuestionableActivityEntity {

    Long getId();
    void setId(Long id);
    Long getTriggerId();
    void setTriggerId(Long triggerId);
    QuestionableActivityTriggerEntity getTrigger();
    void setTrigger(QuestionableActivityTriggerEntity trigger);
    String getBefore();
    void setBefore(String before);
    String getAfter();
    void setAfter(String after);
    Date getActivityDate();
    void setActivityDate(Date activityDate);
    Long getUserId();
    void setUserId(Long userId);
    UserEntity getUser();
    void setUser(UserEntity user);
    Boolean getDeleted();
    void setDeleted(Boolean deleted);
    Long getLastModifiedUser();
    void setLastModifiedUser(Long lastModifiedUser);
    Date getCreationDate();
    void setCreationDate(Date creationDate);
    Date getLastModifiedDate();
    void setLastModifiedDate(Date lastModifiedDate);
}

