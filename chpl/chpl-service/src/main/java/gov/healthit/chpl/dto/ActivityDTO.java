package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.ActivityEntity;
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
    private ActivityConcept concept;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private UserDTO user;

    public ActivityDTO(ActivityEntity entity) {

        this.id = entity.getId();
        this.description = entity.getDescription();
        this.originalData = entity.getOriginalData();
        this.newData = entity.getNewData();
        this.activityDate = entity.getActivityDate();
        this.activityObjectId = entity.getActivityObjectId();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();

        if (entity.getConcept() != null) {
            this.concept = ActivityConcept.valueOf(entity.getConcept().getConcept());
        }
    }
}
