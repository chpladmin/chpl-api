package gov.healthit.chpl.entity;

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
 * Entity object representing the participant_experience_statistics table.
 * @author TYoung
 *
 */
@Entity
@Table(name = "participant_experience_statistics")
public class ParticipantExperienceStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1094674270161664550L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "experience_type_id", nullable = false)
    private Long experienceTypeId;

    @Basic(optional = false)
    @Column(name = "participant_count", nullable = false)
    private Long participantCount;

    @Basic(optional = false)
    @Column(name = "experience_months", nullable = false)
    private Integer experienceMonths;

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
    public ParticipantExperienceStatisticsEntity() {
        //Default Constructor
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public ParticipantExperienceStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantExperienceStatisticsEntity.class;
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
