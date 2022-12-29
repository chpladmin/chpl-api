package gov.healthit.chpl.questionableactivity.dto;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;

public class QuestionableActivityDeveloperDTO extends QuestionableActivityDTO {
    private Long developerId;
    private Developer developer;
    private String reason;

    public QuestionableActivityDeveloperDTO() {
        super();
    }

    public QuestionableActivityDeveloperDTO(QuestionableActivityDeveloperEntity entity) {
        super(entity);
        this.developerId = entity.getDeveloperId();
        this.reason = entity.getReason();
        if (entity.getDeveloper() != null) {
            this.developer = entity.getDeveloper().toDomain();
        }
    }

    public Class<?> getActivityObjectClass() {
        return Developer.class;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "QuestionableActivityDeveloperDTO [developerId=" + developerId + ", developer=" + developer + ", reason="
                + reason + ", getId()=" + getId() + ", getTriggerId()=" + getTriggerId() + ", getActivityDate()="
                + getActivityDate() + ", getBefore()=" + getBefore() + ", getAfter()=" + getAfter() + ", getUserId()="
                + getUserId() + "]";
    }
}
