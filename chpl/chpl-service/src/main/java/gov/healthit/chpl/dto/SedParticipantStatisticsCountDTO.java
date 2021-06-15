package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.statistics.SedParticipantStatisticsCountEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the sed_participant_statistics_count table.
 * @author TYoung
 *
 */
public class SedParticipantStatisticsCountDTO implements Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long sedCount;
    private Long participantCount;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public SedParticipantStatisticsCountDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity SedParticipantStatisticsCount entity
     */
    public SedParticipantStatisticsCountDTO(final SedParticipantStatisticsCountEntity entity) {
        this.setId(entity.getId());
        this.setSedCount(entity.getSedCount());
        this.setParticipantCount(entity.getParticipantCount());
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

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(final Long participantCount) {
        this.participantCount = participantCount;
    }

    public Long getSedCount() {
        return sedCount;
    }

    public void setSedCount(final Long sedCount) {
        this.sedCount = sedCount;
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
