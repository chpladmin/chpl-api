package gov.healthit.chpl.entity.job;

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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.auth.entity.UserEntity;


@Entity
@Table(name = "job")
public class JobEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "job_type_id")
	private Long jobTypeId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "job_type_id", insertable = false, updatable = false)
	private JobTypeEntity jobType;
	
	@Column(name = "user_id")
	private Long userId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	private UserEntity user;
	
	@Column(name = "job_status_id")
	private Long statusId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
 	@Where(clause="deleted <> 'true'")
	@JoinColumn(name = "job_status_id", insertable = false, updatable = false)
	private JobStatusEntity status;
	
	@Column(name = "start_time")
	private Date startTime;
	
	@Column(name = "end_time")
	private Date endTime;
	
	@Column(name = "job_data")
	private String data;
	
 	@OneToMany( fetch = FetchType.LAZY, mappedBy = "jobId"  )
	@Basic( optional = false )
	@Column( name = "job_id", nullable = false  )
 	@Where(clause="deleted <> 'true'")
	private Set<JobMessageEntity> messages = new HashSet<JobMessageEntity>();
	
 	
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

	public Long getJobTypeId() {
		return jobTypeId;
	}

	public void setJobTypeId(Long jobTypeId) {
		this.jobTypeId = jobTypeId;
	}

	public JobTypeEntity getJobType() {
		return jobType;
	}

	public void setJobType(JobTypeEntity jobType) {
		this.jobType = jobType;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
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

	public JobStatusEntity getStatus() {
		return status;
	}

	public void setStatus(JobStatusEntity status) {
		this.status = status;
	}

	public Set<JobMessageEntity> getMessages() {
		return messages;
	}

	public void setMessages(Set<JobMessageEntity> messages) {
		this.messages = messages;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
}
