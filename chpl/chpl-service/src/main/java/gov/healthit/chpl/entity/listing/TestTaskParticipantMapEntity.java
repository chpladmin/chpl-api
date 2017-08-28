package gov.healthit.chpl.entity.listing;

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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.entity.TestTaskEntity;


@Entity
@Table(name = "test_task_participant_map")
public class TestTaskParticipantMapEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "id")
	private Long id;
	
	@Basic( optional = false )
	@Column(name = "test_task_id", nullable = false )	
	private Long testTaskId;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "test_task_id", unique=true, nullable = true, insertable=false, updatable=false)
	@Where(clause="deleted <> 'true'")
	private TestTaskEntity testTask;
	
	@Basic( optional = false )
	@Column(name = "test_participant_id", nullable = false )	
	private Long testParticipantId;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "test_participant_id", unique=true, nullable = true, insertable=false, updatable=false)
	@Where(clause="deleted <> 'true'")
	private TestParticipantEntity testParticipant;

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
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getTestTaskId() {
		return testTaskId;
	}

	public void setTestTaskId(Long testTaskId) {
		this.testTaskId = testTaskId;
	}

	public TestTaskEntity getTestTask() {
		return testTask;
	}

	public void setTestTask(TestTaskEntity testTask) {
		this.testTask = testTask;
	}
}
