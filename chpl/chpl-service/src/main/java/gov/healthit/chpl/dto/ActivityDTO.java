package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO implements Serializable {
    private static final long serialVersionUID = -8364552955791049631L;
    private Long id;
    private String description;
    private String originalData;
    private String newData;
    private Date activityDate;
    private Long activityObjectId;
    private String activityObjectName;
    private String reason;
    private ActivityConcept concept;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private UUID lastModifiedSsoUser;
    private Boolean deleted;
    private User user;
}
