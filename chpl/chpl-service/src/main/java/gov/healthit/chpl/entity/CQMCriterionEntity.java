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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cqm_criterion", schema = "openchpl")
public class CQMCriterionEntity {
	
	
	@Basic( optional = true )
	@Column( name = "cms_id", length = 15  )
	private String cmsId;
	
	@Column(name = "cqm_criterion_type_id", nullable = false )
	private Long cqmCriterionTypeId;
	
	@Basic( optional = true )
	@Column( name = "cqm_domain", length = 250  )
	private String cqmDomain;
	
	@Basic( optional = true )
	@Column( name = "cqm_version_id", length = 10)
	private Long cqmVersionId;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="cqm_version_id", insertable=false, updatable=false )
	private CQMVersionEntity cqmVersion;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column( nullable = false  )
	private Boolean deleted;
	
	@Basic( optional = true )
	@Column( length = 1000  )
	private String description;
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "cqm_criterion_id", nullable = false  )
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = true )
	@Column( name = "nqf_number", length = 50  )
	private String nqfNumber;
	
	@Basic( optional = true )
	@Column( length = 20  )
	private String number;
	
	@Basic( optional = true )
	@Column( length = 250  )
	private String title;

	@Basic( optional = false )
	@Column( name="retired", length = 10  )
	private Boolean retired;
	

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public Long getCqmCriterionTypeId() {
		return cqmCriterionTypeId;
	}

	public void setCqmCriterionTypeId(Long cqmCriterionTypeId) {
		this.cqmCriterionTypeId = cqmCriterionTypeId;
	}

	public String getCqmDomain() {
		return cqmDomain;
	}

	public void setCqmDomain(String cqmDomain) {
		this.cqmDomain = cqmDomain;
	}

	public Long getCqmVersionId() {
		return cqmVersionId;
	}

	public void setCqmVersionId(Long cqmVersion) {
		this.cqmVersionId = cqmVersion;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getNqfNumber() {
		return nqfNumber;
	}

	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public CQMVersionEntity getCqmVersionEntity() {
		return cqmVersion;
	}
	
	public Boolean getRetired() {
		return retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	public String getCqmVersion(){
		
		if (this.cqmVersion != null){
			return this.cqmVersion.getVersion();
		} else {
			return null;
		}
		
	}
	
}
