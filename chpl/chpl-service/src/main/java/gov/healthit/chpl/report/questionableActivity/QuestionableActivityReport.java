package gov.healthit.chpl.report.questionableActivity;

import java.time.LocalDate;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
public class QuestionableActivityReport {
    private String activityType;
    private String developer;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate activityDate;

    /**
     * The CHPL Product Number, developer name, product name, ONC-ACB name, etc depending on the "type" of the activity
     */
    private String relatedItem;
    private Long relatedItemId;
    private String relatedItemUrl;
}
