package gov.healthit.chpl.entity;


import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name = "certification_status")
public class CertificationStatusEntity {

	/** Serial Version UID. */
	private static final long serialVersionUID = -2928065796550377879L;
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "certification_status_id", nullable = false  )
	private Long id;
    
    @Basic( optional = false) 
    @Column (name = "certification_status", nullable = false)
    private String status;
    
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "deleted", nullable = false  )
	private Boolean deleted;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false)
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	/**
	 * Default constructor, mainly for hibernate use.
	 */
	public CertificationStatusEntity() {
		// Default constructor
	} 

	/** Constructor taking a given ID.
	 * @param id to set
	 */
	public CertificationStatusEntity(Long id) {
		this.id = id;
	}
	
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/
	@Transient
	public Class<?> getClassType() {
		return CertificationStatusEntity.class;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
