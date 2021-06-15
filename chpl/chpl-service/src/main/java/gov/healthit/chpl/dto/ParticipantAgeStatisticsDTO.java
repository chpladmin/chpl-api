package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.statistics.ParticipantAgeStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the participant_age_statistics table.
 * @author TYoung
 *
 */
public class ParticipantAgeStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 4347395413448201963L;
    private Long id;
    private Long ageCount;
    private Long testParticipantAgeId;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantAgeStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity ParticipantAgeStatisticsEntity entity
     */
    public ParticipantAgeStatisticsDTO(final ParticipantAgeStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setAgeCount(entity.getAgeCount());
        this.setTestParticipantAgeId(entity.getTestParticipantAgeId());
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

    public Long getAgeCount() {
        return ageCount;
    }

    public void setAgeCount(final Long ageCount) {
        this.ageCount = ageCount;
    }
    public Long getTestParticipantAgeId() {
        return testParticipantAgeId;
    }

    public void setTestParticipantAgeId(final Long testParticipantAgeId) {
        this.testParticipantAgeId = testParticipantAgeId;
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
