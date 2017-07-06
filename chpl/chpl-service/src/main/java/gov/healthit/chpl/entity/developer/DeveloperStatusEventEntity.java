package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
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
@Table(name = "vendor_status_history")
public class DeveloperStatusEventEntity implements Cloneable, Serializable {
	private static final long serialVersionUID = 1730728043307135377L;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "vendor_status_history_id", nullable = false  )
	private Long id;
	
	@Column(name = "vendor_id")
	private Long developerId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", insertable = false, updatable = false)
	private DeveloperEntity developer;
	
	@Column(name = "vendor_status_id")
	private Long developerStatusId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_status_id", insertable = false, updatable = false)
	private DeveloperStatusEntity developerStatus;
	
	@Column(name = "status_date")
	private Date statusDate;
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

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public DeveloperEntity getDeveloper() {
		return developer;
	}

	public void setDeveloper(DeveloperEntity developer) {
		this.developer = developer;
	}

	public Long getDeveloperStatusId() {
		return developerStatusId;
	}

	public void setDeveloperStatusId(Long developerStatusId) {
		this.developerStatusId = developerStatusId;
	}

	public DeveloperStatusEntity getDeveloperStatus() {
		return developerStatus;
	}

	public void setDeveloperStatus(DeveloperStatusEntity developerStatus) {
		this.developerStatus = developerStatus;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
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
}