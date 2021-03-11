package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.util.DeveloperMapper;

public class QuestionableActivityDeveloperDTO extends QuestionableActivityDTO {
    private Long developerId;
    private DeveloperDTO developer;
    private String reason;
    private DeveloperMapper developerMapper;

    public QuestionableActivityDeveloperDTO() {
        super();
        this.developerMapper = new DeveloperMapper();
    }

    public QuestionableActivityDeveloperDTO(QuestionableActivityDeveloperEntity entity) {
        super(entity);
        this.developerId = entity.getDeveloperId();
        this.reason = entity.getReason();
        if (entity.getDeveloper() != null) {
            this.developer = developerMapper.from(entity.getDeveloper());
        }
    }

    public Class<?> getActivityObjectClass() {
        return DeveloperDTO.class;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(DeveloperDTO developer) {
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
