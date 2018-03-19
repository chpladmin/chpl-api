package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;

/**
 * Domain object that represents participant gender statistics used for creating charts.
 * @author TYoung
 *
 */
public class ParticipantGenderStatistics implements Serializable {
    private static final long serialVersionUID = -7580335667077396395L;
    private Long id;
    private Long maleCount;
    private Long femaleCount;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantGenderStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto ParticipantGenderStatisticsDTO object
     */
    public ParticipantGenderStatistics(final ParticipantGenderStatisticsDTO dto) {
        this.id = dto.getId();
        this.maleCount = dto.getMaleCount();
        this.femaleCount = dto.getFemaleCount();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}
