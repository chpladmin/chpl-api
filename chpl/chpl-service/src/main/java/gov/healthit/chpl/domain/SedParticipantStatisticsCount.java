package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.util.Util;


/**
 * Domain object that represents SED counts and participant counts used for creating charts.
 * @author TYoung
 *
 */
public class SedParticipantStatisticsCount implements Serializable {
    private static final long serialVersionUID = 6166234350175390349L;

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
    public SedParticipantStatisticsCount() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto SedParticipantStatisticsCountDTO object
     */
    public SedParticipantStatisticsCount(final SedParticipantStatisticsCountDTO dto) {
        this.id = dto.getId();
        this.sedCount = dto.getSedCount();
        this.participantCount = dto.getParticipantCount();
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

    public Long getSedCount() {
        return sedCount;
    }

    public void setSedCount(final Long sedCount) {
        this.sedCount = sedCount;
    }

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(final Long participantCount) {
        this.participantCount = participantCount;
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
