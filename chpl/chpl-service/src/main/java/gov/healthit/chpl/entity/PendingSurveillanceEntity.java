package gov.healthit.chpl.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "pending_surveillance")
public class PendingSurveillanceEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "surveillance_id_to_replace")
	private String survFriendlyIdToReplace;
	
	@Column(name = "certified_product_unique_id")
	private String certifiedProductUniqueId;
	
	@Column(name = "certified_product_id")
	private Long certifiedProductId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "certified_product_id", insertable = false, updatable = false)
	private CertifiedProductEntity certifiedProduct;
	
	@Column(name = "start_date")
	private Date startDate;
	
	@Column(name = "end_date")
	private Date endDate;
	
	@Column(name = "type_value")
	private String surveillanceType;
	
	@Column(name = "randomized_sites_used")
	private Integer numRandomizedSites;
	
	@Column(name = "deleted")
	private Boolean deleted;
	
	@Column(name = "last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;
	
	@Column(name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;
	
 	@OneToMany( fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceId"  )
	@Basic( optional = false )
	@Column( name = "pending_surveillance_id", nullable = false  )
	private Set<PendingSurveillanceRequirementEntity> surveilledRequirements = new HashSet<PendingSurveillanceRequirementEntity>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCertifiedProductUniqueId() {
		return certifiedProductUniqueId;
	}

	public void setCertifiedProductUniqueId(String certifiedProductUniqueId) {
		this.certifiedProductUniqueId = certifiedProductUniqueId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getSurveillanceType() {
		return surveillanceType;
	}

	public void setSurveillanceType(String surveillanceType) {
		this.surveillanceType = surveillanceType;
	}

	public Integer getNumRandomizedSites() {
		return numRandomizedSites;
	}

	public void setNumRandomizedSites(Integer numRandomizedSites) {
		this.numRandomizedSites = numRandomizedSites;
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

	public Set<PendingSurveillanceRequirementEntity> getSurveilledRequirements() {
		return surveilledRequirements;
	}

	public void setSurveilledRequirements(Set<PendingSurveillanceRequirementEntity> surveilledRequirements) {
		this.surveilledRequirements = surveilledRequirements;
	}

	public CertifiedProductEntity getCertifiedProduct() {
		return certifiedProduct;
	}

	public void setCertifiedProduct(CertifiedProductEntity certifiedProduct) {
		this.certifiedProduct = certifiedProduct;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public String getSurvFriendlyIdToReplace() {
		return survFriendlyIdToReplace;
	}

	public void setSurvFriendlyIdToReplace(String survFriendlyIdToReplace) {
		this.survFriendlyIdToReplace = survFriendlyIdToReplace;
	}
}
