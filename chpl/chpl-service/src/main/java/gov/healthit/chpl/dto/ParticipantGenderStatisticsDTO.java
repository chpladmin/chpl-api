package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ParticipantGenderStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the participant_gender_statistics table.
 * @author TYoung
 *
 */
public class ParticipantGenderStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long maleCount;
    private Long femaleCount;
    private Long unknownCount;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantGenderStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity ParticipantGenderStatisticsEntity entity
     */
    public ParticipantGenderStatisticsDTO(final ParticipantGenderStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setMaleCount(entity.getMaleCount());
        this.setFemaleCount(entity.getFemaleCount());
        this.setUnknownCount(entity.getUnknownCount());
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

    public Long getMaleCount() {
        return maleCount;
    }

    public void setMaleCount(final Long maleCount) {
        this.maleCount = maleCount;
    }

    public Long getFemaleCount() {
        return femaleCount;
    }

    public void setFemaleCount(final Long femaleCount) {
        this.femaleCount = femaleCount;
    }

    public Long getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(Long unknownCount) {
        this.unknownCount = unknownCount;
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
