package gov.healthit.chpl.questionableactivity.dto;

import java.io.Serializable;

import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityTriggerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionableActivityTriggerDTO implements Serializable {
    private static final long serialVersionUID = -7627129167248983500L;

    private Long id;
    private String name;
    private String level;

    public QuestionableActivityTriggerDTO(QuestionableActivityTriggerEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.level = entity.getLevel();
    }
}
