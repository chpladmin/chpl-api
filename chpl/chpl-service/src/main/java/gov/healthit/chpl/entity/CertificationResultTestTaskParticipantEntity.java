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
@Table(name = "certification_result_test_task_participant")
public class CertificationResultTestTaskParticipantEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "certification_result_test_task_participant_id")
	private Long id;
	
	@Basic( optional = false )
	@Column(name = "certification_result_test_task_id", nullable = false )	
	private Long certificationResultTestTaskId;

	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_result_test_task_id", unique=true, nullable = true, insertable=false, updatable=false)
	private CertificationResultTestTaskEntity certTestTask;
	
	@Basic( optional = false )
	@Column(name = "test_participant_id", nullable = false )	
	private Long testParticipantId;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "test_participant_id", unique=true, nullable = true, insertable=false, updatable=false)
	private TestParticipantEntity testParticipant;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	protected Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	protected Boolean deleted;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	protected Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	protected Long lastModifiedUser;
	
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

	public Long getTestParticipantId() {
		return testParticipantId;
	}

	public void setTestParticipantId(Long testParticipantId) {
		this.testParticipantId = testParticipantId;
	}

	public TestParticipantEntity getTestParticipant() {
		return testParticipant;
	}

	public void setTestParticipant(TestParticipantEntity testParticipantEntity) {
		this.testParticipant = testParticipantEntity;
	}

	public Long getCertificationResultTestTaskId() {
		return certificationResultTestTaskId;
	}

	public void setCertificationResultTestTaskId(Long certificationResultTestTaskId) {
		this.certificationResultTestTaskId = certificationResultTestTaskId;
	}

	public CertificationResultTestTaskEntity getCertTestTask() {
		return certTestTask;
	}

	public void setCertTestTask(CertificationResultTestTaskEntity certTestTask) {
		this.certTestTask = certTestTask;
	}
}
