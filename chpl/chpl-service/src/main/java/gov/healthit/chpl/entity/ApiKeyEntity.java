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
@Table(name="api_key")
public class ApiKeyEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "api_key_id", nullable = false )
	private Long id;
	
	@Basic(optional = false)
	@Column(name = "api_key")
	private String apiKey;
	
	@Basic(optional = false)
	@Column(name = "email")
	private String email;
	
	@Basic(optional = false)
	@Column(name = "name_organization")
	private String nameOrganization;
	
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

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNameOrganization() {
		return nameOrganization;
	}

	public void setNameOrganization(String nameOrganization) {
		this.nameOrganization = nameOrganization;
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
