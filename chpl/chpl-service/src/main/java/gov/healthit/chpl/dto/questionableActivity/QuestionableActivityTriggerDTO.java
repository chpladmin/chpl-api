package gov.healthit.chpl.dto.questionableActivity;

import java.io.Serializable;

import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityTriggerEntity;

public class QuestionableActivityTriggerDTO implements Serializable {
    private static final long serialVersionUID = -7627129167248983500L;

    private Long id;
    private String name;

    public QuestionableActivityTriggerDTO() {}
    public QuestionableActivityTriggerDTO(final QuestionableActivityTriggerEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
    public Long getId() {
        return id;
    }
    public void setId(final Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }
}
