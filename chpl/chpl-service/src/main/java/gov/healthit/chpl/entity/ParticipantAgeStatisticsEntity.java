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

@Entity
@Table(name = "participant_age_statistics")
public class ParticipantAgeStatisticsEntity implements Serializable {
    private static final long serialVersionUID = -3608777880397004236L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "age_count", nullable = false)
    private Long ageCount;
    
    @Basic(optional = false)
    @Column(name = "test_participant_age_id", nullable = false)
    private Long testParticipantAgeId;
    
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

    public ParticipantAgeStatisticsEntity() {
        //Default Constructor
    }
    
    public ParticipantAgeStatisticsEntity(Long id) {
        this.id = id;
    }
    
    @Transient
    public Class<?> getClassType() {
        return SedParticipantStatisticsCountEntity.class;
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
    
    public Long getTestParticipantAgeId() {
        return testParticipantAgeId;
    }
    
    public void setTestParticipantAgeId(final Long testParticipantAgeId) {
        this.testParticipantAgeId = testParticipantAgeId;
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
