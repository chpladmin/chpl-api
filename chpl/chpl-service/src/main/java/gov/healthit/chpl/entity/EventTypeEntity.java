package gov.healthit.chpl.entity;

import gov.healthit.chpl.entity.CertificationEventEntity;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.proxy.HibernateProxy;


@Entity
@Table(name = "event_type")
public class EventTypeEntity implements Serializable {

	/** Serial Version UID. */
	private static final long serialVersionUID = 5446425777535537951L;

	
    @Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventTypeEvent_type_idGenerator")
	@Basic( optional = false )
	@Column( name = "event_type_id", nullable = false  )
	@SequenceGenerator(name = "eventTypeEvent_type_idGenerator", sequenceName = "event_type_event_type_id_seq")
	private Long id;
	
 	@OneToMany( fetch = FetchType.LAZY, mappedBy = "eventType"  ) 	
	@Basic( optional = false )
	@Column( name = "event_type_id", nullable = false  )
	private Set<CertificationEventEntity> certificationEvents = new HashSet<CertificationEventEntity>();
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	private Boolean deleted;
	
	@Basic( optional = false )
	@Column( nullable = false, length = 250  )
	private String description;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( nullable = false, length = 50  )
	private String name;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<CertificationEventEntity> getCertificationEvents() {
		return certificationEvents;
	}

	public void setCertificationEvents(Set<CertificationEventEntity> certificationEvents) {
		this.certificationEvents = certificationEvents;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
