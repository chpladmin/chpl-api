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
 * Entity object representing the participant_gender_statistics table.
 * @author TYoung
 *
 */
@Entity
@Table(name = "participant_gender_statistics")
public class ParticipantGenderStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1313677047965534572L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "male_count", nullable = false)
    private Long maleCount;

    @Basic(optional = false)
    @Column(name = "female_count", nullable = false)
    private Long femaleCount;

    @Basic(optional = false)
    @Column(name = "unknown_count", nullable = false)
    private Long unknownCount;

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
    public ParticipantGenderStatisticsEntity() {
        //Default Constructor
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public ParticipantGenderStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantGenderStatisticsEntity.class;
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

    public void setUnknownCount(final Long unknownCount) {
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
