    package gov.healthit.chpl.entity.statistics;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.util.Util;

/**
 * Entity object representing the participant_education_statistics table.
 * @author TYoung
 *
 */
@Entity
@Table(name = "participant_education_statistics")
public class ParticipantEducationStatisticsEntity implements Serializable {
    private static final long serialVersionUID = -4258273713908999510L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "education_count", nullable = false)
    private Long educationCount;

    @Basic(optional = false)
    @Column(name = "education_type_id", nullable = false)
    private Long educationTypeId;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ParticipantEducationStatisticsEntity() {
        //Default Constructor
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public ParticipantEducationStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantEducationStatisticsEntity.class;
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
