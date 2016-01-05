package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "certification_event")
public class CertificationEventEntity implements Cloneable, Serializable {

	/** Serial Version UID. */
	private static final long serialVersionUID = 4174889617079658144L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "certification_event_id", nullable = false  )
	private Long id;
	
	
	@Basic( optional = false )
	@Column(name = "certified_product_id", nullable = false )
	private Long certifiedProductId;
    

	@Basic( optional = true )
	@Column( length = 250  )
	private String city;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	private Boolean deleted;
	
	@Basic( optional = false )
	@Column( name = "event_date", nullable = false  )
	private Date eventDate;
	
	/*
	@ManyToOne(fetch = FetchType.LAZY )
	@Basic( optional = false )
	@JoinColumn( name = "event_type_id", nullable = false, insertable=false, updatable=false)
	private EventTypeEntity eventType;
	*/
	
	@Basic( optional = false )
	@Column( name = "event_type_id", nullable = false )
	private Long eventTypeId;

	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = true )
	@Column( length = 25  )
	private String state;

	
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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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
	/*
	public EventTypeEntity getEventType() {
		return eventType;
	}
	*/
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public Long getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(Long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}
	
}