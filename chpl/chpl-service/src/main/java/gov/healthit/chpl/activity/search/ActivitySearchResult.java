package gov.healthit.chpl.activity.search;

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
public class ActivitySearchResult implements Serializable {
    private static final long serialVersionUID = -815386136021172537L;

    private Long id;
    private String concept;
    private Long objectId;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime activityDate;
    private String reason;
    private String before;
    private String after;
    private String username;
}
