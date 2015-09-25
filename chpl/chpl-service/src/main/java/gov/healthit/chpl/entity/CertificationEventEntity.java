package gov.healthit.chpl.entity;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="certification_event")
public class CertificationEventEntity {
	
    @Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "certificationEventCertification_event_idGenerator")
	@Basic( optional = false )
	@Column( name = "certification_event_id", nullable = false  )
	@SequenceGenerator(name = "certificationEventCertification_event_idGenerator", sequenceName = "certification_event_certification_event_id_seq")
	private Long id;
	
    @Basic (optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;
	
    @Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "event_type_id", unique=true, nullable = true)
	private EventTypeEntity eventType;
    
    @Basic(optional = true)
    @Column(name = "event_date", nullable = false)
    private Date eventDate;
    
    @Basic (optional = false)
    @Column(name = "city", nullable = false)
    private String city;
    
    @Basic (optional = false)
    @Column(name = "state", nullable = false)
    private String state;
    
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
	public EventTypeEntity getEventType() {
		return eventType;
	}
	public void setEventType(EventTypeEntity eventType) {
		this.eventType = eventType;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
