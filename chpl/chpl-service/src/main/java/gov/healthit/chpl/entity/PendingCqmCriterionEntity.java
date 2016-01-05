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
@Table(name="pending_cqm_criterion")
public class PendingCqmCriterionEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "pending_cqm_criterion_id", nullable = false  )
	private Long id;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "cqm_criterion_id", unique=true, nullable = true)
	private CQMCriterionEntity mappedCriterion;
	
	@Column(name="pending_certified_product_id")
	private Long pendingCertifiedProductId;

	@Column(name = "meets_criteria")
	private Boolean meetsCriteria;

	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( name = "deleted", nullable = false  )
	private Boolean deleted;
	
//	@Transient private String cqmNumber;
//	@Transient private String cmsId;
//	@Transient private String title;
//	@Transient private String nqfNumber;
//	@Transient private String version;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Boolean getMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(Boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
	}

//	public String getCqmNumber() {
//		return cqmNumber;
//	}
//
//	public void setCqmNumber(String cqmNumber) {
//		this.cqmNumber = cqmNumber;
//	}
//
//	public String getCmsId() {
//		return cmsId;
//	}
//
//	public void setCmsId(String cmsId) {
//		this.cmsId = cmsId;
//	}
//
//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public String getNqfNumber() {
//		return nqfNumber;
//	}
//
//	public void setNqfNumber(String nqfNumber) {
//		this.nqfNumber = nqfNumber;
//	}
//
//	public String getVersion() {
//		return version;
//	}
//
//	public void setVersion(String version) {
//		this.version = version;
//	}

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

	public CQMCriterionEntity getMappedCriterion() {
		return mappedCriterion;
	}

	public void setMappedCriterion(CQMCriterionEntity mappedCriterion) {
		this.mappedCriterion = mappedCriterion;
	}
}
