package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;

public class AuditedEntity {
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	private Boolean deleted;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
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
