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
@Table(name="openchpl.api_key_activity")
public class ApiKeyActivityEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "api_key_activity_id", nullable = false )
	private Long id;
	
	@Basic(optional=false)
	@Column(name = "api_key_id")
	private Long apiKeyId;
	
	@Basic(optional=false)
	@Column(name = "api_call_path" )
	private String apiCallPath;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( name = "deleted", nullable = false )
	private Boolean deleted;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getApiKeyId() {
		return apiKeyId;
	}

	public void setApiKeyId(Long apiKeyId) {
		this.apiKeyId = apiKeyId;
	}

	public String getApiCallPath() {
		return apiCallPath;
	}

	public void setApiCallPath(String apiCallPath) {
		this.apiCallPath = apiCallPath;
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

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
}
