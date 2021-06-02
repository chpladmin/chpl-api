package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.statistics.ParticipantEducationStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the participant_education_statistics table.
 * @author TYoung
 *
 */
public class ParticipantEducationStatisticsDTO implements Serializable {
    private static final long serialVersionUID = -8808520961352931335L;

    private Long id;
    private Long educationCount;
    private Long educationTypeIdId;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantEducationStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity ParticipantEducationStatisticsEntity entity
     */
    public ParticipantEducationStatisticsDTO(final ParticipantEducationStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setEducationCount(entity.getEducationCount());
        this.setEducationTypeId(entity.getEducationTypeId());
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getEducationCount() {
        return educationCount;
    }

    public void setEducationCount(final Long educationCount) {
        this.educationCount = educationCount;
    }
    public Long getEducationTypeId() {
        return educationTypeIdId;
    }

    public void setEducationTypeId(final Long educationTypeIdId) {
        this.educationTypeIdId = educationTypeIdId;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
    
    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

}
