package gov.healthit.chpl.questionableactivity.domain;

import java.util.Date;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class QuestionableActivityBase {
    private Long id;
    private Long activityId;
    private QuestionableActivityTrigger trigger;
    private String before;
    private String after;
    private Date activityDate;
    private Long userId;
    private UserDTO user;

    public abstract Class<?> getActivityObjectClass();

    public QuestionableActivityBase(QuestionableActivityBaseEntity entity) {
        this.id = entity.getId();
        this.activityId = entity.getActivityId();
        if (entity.getTrigger() != null) {
            this.trigger = entity.getTrigger().toDomain();
        }
        this.before = entity.getBefore();
        this.after = entity.getAfter();
        this.activityDate = entity.getActivityDate();
        this.userId = entity.getUserId();
    }
}
