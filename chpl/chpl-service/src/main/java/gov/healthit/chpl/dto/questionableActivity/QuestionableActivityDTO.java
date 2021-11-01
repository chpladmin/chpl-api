package gov.healthit.chpl.dto.questionableActivity;

import java.util.Date;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityEntity;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class QuestionableActivityDTO {
    private Long id;
    private Long triggerId;
    private QuestionableActivityTriggerDTO trigger;
    private String before;
    private String after;
    private Date activityDate;
    private Long userId;
    private UserDTO user;

    public abstract Class<?> getActivityObjectClass();

    public QuestionableActivityDTO(QuestionableActivityEntity entity) {
        this.id = entity.getId();
        this.triggerId = entity.getTriggerId();
        if (entity.getTrigger() != null) {
            this.trigger = new QuestionableActivityTriggerDTO(entity.getTrigger());
        }
        this.before = entity.getBefore();
        this.after = entity.getAfter();
        this.activityDate = entity.getActivityDate();
        this.userId = entity.getUserId();
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

    public Date getActivityDate() {
        return Util.getNewDate(activityDate);
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = Util.getNewDate(activityDate);
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
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
