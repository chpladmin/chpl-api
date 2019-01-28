package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.util.Util;

/**
 * Domain object that represents participant age statistics used for creating charts.
 * @author TYoung
 *
 */
public class ParticipantAgeStatistics implements Serializable {
    private static final long serialVersionUID = -3740031503734395401L;
    private Long id;
    private Long ageCount;
    private Long testParticipantAgeId;
    private String ageRange;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantAgeStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto ParticipantAgeStatisticsDTO object
     */
    public ParticipantAgeStatistics(final ParticipantAgeStatisticsDTO dto) {
        this.id = dto.getId();
        this.ageCount = dto.getAgeCount();
        this.testParticipantAgeId = dto.getTestParticipantAgeId();
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

    public Long getAgeCount() {
        return ageCount;
    }

    public void setAgeCount(final Long ageCount) {
        this.ageCount = ageCount;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(final String ageRange) {
        this.ageRange = ageRange;
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
