package gov.healthit.chpl.questionableactivity.dto;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class QuestionableActivityDeveloperDTO extends QuestionableActivity {
    private Long developerId;
    private Developer developer;
    private String reason;

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
}
