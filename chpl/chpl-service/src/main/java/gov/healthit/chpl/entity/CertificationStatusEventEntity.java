package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "certification_status_event")
public class CertificationStatusEventEntity implements Cloneable, Serializable {

	/** Serial Version UID. */
	private static final long serialVersionUID = 4174889617079658144L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "certification_status_event_id")
	private Long id;
	
	@Column(name = "certified_product_id")
	private Long certifiedProductId;
	
	@Column( name = "event_date")
	private Date eventDate;
	
	@Column(name = "certification_status_id")
	private Long certificationStatusId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_status_id", insertable = false, updatable = false)
	private CertificationStatusEntity certificationStatus;
	
	@Column( name = "deleted")
	private Boolean deleted;

	@Column( name = "last_modified_user")
	private Long lastModifiedUser;
	
	@Column( name = "last_modified_date", insertable=false, updatable=false  )
	private Date lastModifiedDate;
	
	@Column( name = "creation_date", insertable=false, updatable=false)
	private Date creationDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
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

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
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

	public Long getCertificationStatusId() {
		return certificationStatusId;
	}

	public void setCertificationStatusId(Long certificationStatusId) {
		this.certificationStatusId = certificationStatusId;
	}

	public CertificationStatusEntity getCertificationStatus() {
		return certificationStatus;
	}

	public void setCertificationStatus(CertificationStatusEntity certificationStatus) {
		this.certificationStatus = certificationStatus;
	}
}