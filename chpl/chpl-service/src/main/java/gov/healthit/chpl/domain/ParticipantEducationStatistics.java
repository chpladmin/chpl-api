package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.util.Util;

/**
 * Domain object that represents participant education statistics used for creating charts.
 * @author TYoung
 *
 */
public class ParticipantEducationStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long educationCount;
    private Long educationTypeId;
    private String education;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantEducationStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto ParticipantEducationStatisticsDTO object
     */
    public ParticipantEducationStatistics(final ParticipantEducationStatisticsDTO dto) {
        this.id = dto.getId();
        this.educationCount = dto.getEducationCount();
        this.educationTypeId = dto.getEducationTypeId();
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

    public Long getEducationCount() {
        return educationCount;
    }

    public void setEducationCount(final Long educationCount) {
        this.educationCount = educationCount;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(final String education) {
        this.education = education;
    }

    public Long getEducationTypeId() {
        return educationTypeId;
    }

    public void setEducationTypeId(final Long educationTypeId) {
        this.educationTypeId = educationTypeId;
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
