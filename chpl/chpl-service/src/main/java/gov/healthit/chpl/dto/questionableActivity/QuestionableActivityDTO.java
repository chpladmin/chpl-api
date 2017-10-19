package gov.healthit.chpl.dto.questionableActivity;

import java.util.Date;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityEntity;

public abstract class QuestionableActivityDTO {
    private Long id;
    private Long triggerId;
    private QuestionableActivityTriggerDTO trigger;
    private String message;
    private Date activityDate;
    private Long userId;
    private UserDTO user;
    
    public abstract Class<?> getActivityObjectClass();

    public QuestionableActivityDTO() {}
    public QuestionableActivityDTO(QuestionableActivityEntity entity) {
        this.id = entity.getId();
        this.triggerId = entity.getTriggerId();
        if(entity.getTrigger() != null) {
            this.trigger = new QuestionableActivityTriggerDTO(entity.getTrigger());
        }
        this.message = entity.getMessage();
        this.activityDate = entity.getActivityDate();
        this.userId = entity.getUserId();
        if(entity.getUser() != null) {
            this.user = new UserDTO(entity.getUser());
        }
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public QuestionableActivityTriggerDTO getTrigger() {
        return trigger;
    }

    public void setTrigger(QuestionableActivityTriggerDTO trigger) {
        this.trigger = trigger;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
    
}
