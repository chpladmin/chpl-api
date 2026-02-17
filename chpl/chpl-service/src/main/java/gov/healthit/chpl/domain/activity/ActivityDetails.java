package gov.healthit.chpl.domain.activity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRawValue;
import tools.jackson.databind.JsonNode;

import gov.healthit.chpl.domain.auth.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ActivityDetails implements Serializable {
    private static final long serialVersionUID = -2870230141890372965L;

    private Long id;
    private String description;
    @JsonRawValue
    private JsonNode originalData;
    @JsonRawValue
    private JsonNode newData;
    private Date activityDate;
    private Long activityObjectId;
    private UUID activityObjectUuid;
    private ActivityConcept concept;
    private User responsibleUser;
}
