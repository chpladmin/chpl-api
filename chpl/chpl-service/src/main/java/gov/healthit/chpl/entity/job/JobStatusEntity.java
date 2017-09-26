package gov.healthit.chpl.entity.job;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;


@Entity
@Table(name = "job_status")
public class JobStatusEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	@Type(type = "gov.healthit.chpl.entity.job.PostgresJobStatusType" , parameters ={@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.job.JobStatusType")} )
	private JobStatusType status;

	@Column(name = "percent_complete")
	private Integer percentComplete;
	
	@Column( name = "deleted")
	private Boolean deleted;
	
	@Column( name = "last_modified_user")
	private Long lastModifiedUser;
	
	@Column( name = "creation_date", insertable = false, updatable = false  )
	private Date creationDate;
	
	@Column( name = "last_modified_date", insertable = false, updatable = false )
	private Date lastModifiedDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JobStatusType getStatus() {
		return status;
	}

	public void setStatus(JobStatusType status) {
		this.status = status;
	}

	public Integer getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Integer percentComplete) {
		this.percentComplete = percentComplete;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
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

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
