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
import javax.persistence.Table;


@Entity
@Table(name = "test_standard")
public class TestStandardEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "test_standard_id")
	private Long id;
	
	@Column( name = "number", nullable = false  )
	private String name;
	
	@Column( name = "name", nullable = false  )
	private String description;
	
	@Column(name = "certification_edition_id")
	private Long certificationEditionId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
	private CertificationEditionEntity certificationEdition;
	
	@Column( name="deleted", nullable = false  )
	protected Boolean deleted;

	@Column( name = "last_modified_user", nullable = false )
	protected Long lastModifiedUser;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public CertificationEditionEntity getCertificationEdition() {
		return certificationEdition;
	}

	public void setCertificationEdition(CertificationEditionEntity certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
}
