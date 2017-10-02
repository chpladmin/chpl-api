package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="pending_certification_result_test_task_participant")
public class PendingCertificationResultTestTaskParticipantEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "pending_certification_result_test_task_participant_id", nullable = false  )
	private Long id;

	@Basic( optional = false )
	@Column(name = "pending_certification_result_test_task_id", nullable = false )
	private Long pendingCertificationResultTestTaskId;

	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "pending_certification_result_test_task_id", unique = true, nullable = true, insertable = false, updatable = false)
	private PendingCertificationResultTestTaskEntity certTestTask;

	@Basic( optional = false )
	@Column(name = "pending_test_participant_id", nullable = false )
	private Long pendingTestParticipantId;

	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "pending_test_participant_id", unique = true, nullable = true, insertable = false, updatable = false)
	private PendingTestParticipantEntity testParticipant;

	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;

	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;

	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;

	@Basic( optional = false )
	@Column( name = "deleted", nullable = false  )
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getPendingCertificationResultTestTaskId() {
		return pendingCertificationResultTestTaskId;
	}

	public void setPendingCertificationResultTestTaskId(Long pendingCertificationResultTestTaskId) {
		this.pendingCertificationResultTestTaskId = pendingCertificationResultTestTaskId;
	}

	public PendingCertificationResultTestTaskEntity getCertTestTask() {
		return certTestTask;
	}

	public void setCertTestTask(PendingCertificationResultTestTaskEntity certTestTask) {
		this.certTestTask = certTestTask;
	}

	public Long getPendingTestParticipantId() {
		return pendingTestParticipantId;
	}

	public void setPendingTestParticipantId(Long pendingTestParticipantId) {
		this.pendingTestParticipantId = pendingTestParticipantId;
	}

	public PendingTestParticipantEntity getTestParticipant() {
		return testParticipant;
	}

	public void setTestParticipant(PendingTestParticipantEntity testParticipant) {
		this.testParticipant = testParticipant;
	}
}
