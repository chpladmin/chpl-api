package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ParticipantExperienceStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the participant_experience_statistics table.
 * @author TYoung
 *
 */
public class ParticipantExperienceStatisticsDTO implements Serializable {
    private static final long serialVersionUID = -7077206153838478208L;

    private Long id;
    private Long experienceTypeId;
    private Long participantCount;
    private Integer experienceMonths;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantExperienceStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity ParticipantExperienceStatisticsEntity entity
     */
    public ParticipantExperienceStatisticsDTO(final ParticipantExperienceStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setExperienceTypeId(entity.getExperienceTypeId());
        this.setParticipantCount(entity.getParticipantCount());
        this.setExperienceMonths(entity.getExperienceMonths());
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

    public Long getExperienceTypeId() {
        return experienceTypeId;
    }

    public void setExperienceTypeId(final Long experienceTypeId) {
        this.experienceTypeId = experienceTypeId;
    }

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(final Long participantCount) {
        this.participantCount = participantCount;
    }
    public Integer getExperienceMonths() {
        return experienceMonths;
    }

    public void setExperienceMonths(final Integer experienceMonths) {
        this.experienceMonths = experienceMonths;
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
