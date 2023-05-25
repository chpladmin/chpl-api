package gov.healthit.chpl.questionableactivity.domain;

import gov.healthit.chpl.domain.Developer;
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
public class QuestionableActivityDeveloper extends QuestionableActivityBase {
    private Long developerId;
    private Developer developer;
    private String reason;

    public Class<?> getActivityObjectClass() {
        return Developer.class;
    }
}
