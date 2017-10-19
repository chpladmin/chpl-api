package gov.healthit.chpl.entity.questionableActivity;

import java.util.Date;

import gov.healthit.chpl.auth.entity.UserEntity;

public interface QuestionableActivityEntity {

    public Long getId();
    public void setId(final Long id);
    public Long getTriggerId();
    public void setTriggerId(Long triggerId);
    public QuestionableActivityTriggerEntity getTrigger();
    public void setTrigger(QuestionableActivityTriggerEntity trigger);
    public String getMessage();
    public void setMessage(String message);
    public Date getActivityDate();
    public void setActivityDate(Date activityDate);
    public Long getUserId();
    public void setUserId(Long userId);
    public UserEntity getUser();
    public void setUser(UserEntity user);
    public Boolean getDeleted();
    public void setDeleted(Boolean deleted);
    public Long getLastModifiedUser();
    public void setLastModifiedUser(Long lastModifiedUser);
    public Date getCreationDate();
    public void setCreationDate(Date creationDate);
    public Date getLastModifiedDate();
    public void setLastModifiedDate(Date lastModifiedDate);
}

