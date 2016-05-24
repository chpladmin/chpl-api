package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="pending_certification_result_test_functionality")
public class PendingCertificationResultTestFunctionalityEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "pending_certification_result_test_functionality_id", nullable = false  )
	private Long id;

	@Basic( optional = false )
	@Column(name = "pending_certification_result_id", nullable = false )	
	private Long pendingCertificationResultId;

	@Column(name = "test_functionality_id")
	private Long testFunctionalityId;
	
	@Column(name = "test_functionality_number")
	private String testFunctionalityNumber;
	
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

	public Long getPendingCertificationResultId() {
		return pendingCertificationResultId;
	}

	public void setPendingCertificationResultId(Long pendingCertificationResultId) {
		this.pendingCertificationResultId = pendingCertificationResultId;
	}

	public Long getTestFunctionalityId() {
		return testFunctionalityId;
	}

	public void setTestFunctionalityId(Long testFunctionalityId) {
		this.testFunctionalityId = testFunctionalityId;
	}

	public String getTestFunctionalityNumber() {
		return testFunctionalityNumber;
	}

	public void setTestFunctionalityNumber(String testFunctionalityName) {
		this.testFunctionalityNumber = testFunctionalityName;
	}
}
