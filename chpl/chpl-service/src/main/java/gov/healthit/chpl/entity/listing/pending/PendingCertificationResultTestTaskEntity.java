package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_certification_result_test_task")
public class PendingCertificationResultTestTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_certification_result_test_task_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Long pendingCertificationResultId;

    @Basic(optional = false)
    @Column(name = "pending_test_task_id", nullable = false)
    private Long pendingTestTaskId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_test_task_id", unique = true, nullable = true, insertable = false, updatable = false)
    private PendingTestTaskEntity testTask;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultTestTaskId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_test_task_id", nullable = false)
    private Set<PendingCertificationResultTestTaskParticipantEntity> testParticipants = new HashSet<PendingCertificationResultTestTaskParticipantEntity>();

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Long getPendingCertificationResultId() {
        return pendingCertificationResultId;
    }

    public void setPendingCertificationResultId(final Long pendingCertificationResultId) {
        this.pendingCertificationResultId = pendingCertificationResultId;
    }

    public Long getPendingTestTaskId() {
        return pendingTestTaskId;
    }

    public void setPendingTestTaskId(final Long pendingTestTaskId) {
        this.pendingTestTaskId = pendingTestTaskId;
    }

    public PendingTestTaskEntity getTestTask() {
        return testTask;
    }

    public void setTestTask(final PendingTestTaskEntity testTask) {
        this.testTask = testTask;
    }

    public Set<PendingCertificationResultTestTaskParticipantEntity> getTestParticipants() {
        return testParticipants;
    }

    public void setTestParticipants(final Set<PendingCertificationResultTestTaskParticipantEntity> testParticipants) {
        this.testParticipants = testParticipants;
    }
}
