package gov.healthit.chpl.questionableactivity.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionableActivity implements Serializable {
    private static final long serialVersionUID = -8153861360218726537L;

    private String triggerLevel;
    private String triggerName;
    private Long activityId;
    private String before;
    private String after;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime activityDate;
    private Long userId;
    private String username;
    private String certificationStatusChangeReason;
    private String reason;
    private Long developerId;
    private String developerName;
    private Long productId;
    private String productName;
    private Long versionId;
    private String versionName;
    private Long listingId;
    private String chplProductNumber;
    private Long acbId;
    private String acbName;
    private Long certificationStatusId;
    private String certificationStatusName;
    private Long certificationCriterionId;
}
