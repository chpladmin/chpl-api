package gov.healthit.chpl.questionableactivity.dto;

import java.util.Date;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class QuestionableActivityDTO {
    private Long id;
    private Long triggerId;
    private Long activityId;
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
        this.activityId = entity.getActivityId();
        if (entity.getTrigger() != null) {
            this.trigger = new QuestionableActivityTriggerDTO(entity.getTrigger());
        }
        this.before = entity.getBefore();
        this.after = entity.getAfter();
        this.activityDate = entity.getActivityDate();
        this.userId = entity.getUserId();
    }
}
