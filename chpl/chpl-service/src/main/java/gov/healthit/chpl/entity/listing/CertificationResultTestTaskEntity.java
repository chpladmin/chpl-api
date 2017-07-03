package gov.healthit.chpl.entity.listing;

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

import gov.healthit.chpl.entity.TestTaskEntity;


@Entity
@Table(name = "certification_result_test_task")
public class CertificationResultTestTaskEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "certification_result_test_task_id")
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "certification_result_id", nullable = false  )
	private Long certificationResultId;
	
	@Column(name = "test_task_id")
	private Long testTaskId;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "test_task_id", unique=true, nullable = true, insertable=false, updatable=false)
	private TestTaskEntity testTask;
	
 	@OneToMany( fetch = FetchType.LAZY, mappedBy = "certificationResultTestTaskId"  )
	@Basic( optional = false )
	@Column( name = "certification_result_test_task_id", nullable = false  )
	private Set<CertificationResultTestTaskParticipantEntity> testParticipants = new HashSet<CertificationResultTestTaskParticipantEntity>();
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
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

	public Long getTestTaskId() {
		return testTaskId;
	}

	public void setTestTaskId(Long testTaskId) {
		this.testTaskId = testTaskId;
	}

	public TestTaskEntity getTestTask() {
		return testTask;
	}

	public void setTestTask(TestTaskEntity testTaskEntity) {
		this.testTask = testTaskEntity;
	}

	public Set<CertificationResultTestTaskParticipantEntity> getTestParticipants() {
		return testParticipants;
	}

	public void setTestParticipants(Set<CertificationResultTestTaskParticipantEntity> testParticipants) {
		this.testParticipants = testParticipants;
	}
}
