package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.util.Util;

/**
 * Domain object that represents participant experience statistics used for creating charts.
 * @author TYoung
 *
 */
public class ParticipantExperienceStatistics implements Serializable {
    private static final long serialVersionUID = -761630337976327445L;

    private Long id;
    private Long participantCount;
    private Integer experienceMonths;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantExperienceStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto ParticipantExperienceStatisticsDTO object
     */
    public ParticipantExperienceStatistics(final ParticipantExperienceStatisticsDTO dto) {
        this.id = dto.getId();
        this.participantCount = dto.getParticipantCount();
        this.experienceMonths = dto.getExperienceMonths();
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

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(final Long participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getExperienceMonths() {
        return experienceMonths;
    }

    public void setEducationTypeId(final Integer experienceMonths) {
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
