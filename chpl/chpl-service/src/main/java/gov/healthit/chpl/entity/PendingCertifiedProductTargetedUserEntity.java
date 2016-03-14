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
@Table(name = "pending_certified_product_targeted_user")
public class PendingCertifiedProductTargetedUserEntity {
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "pending_certified_product_qms_standard_id", nullable = false  )
	private Long id;
    
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "pending_certified_product_id", unique=true, nullable = true, insertable = false, updatable = false)
	private PendingCertifiedProductEntity mappedProduct;
	
	@Column(name="pending_certified_product_id")
	private Long pendingCertifiedProductId;
	
    @Column(name = "targeted_user_id")
    private Long targetedUserId;
    
    @Column(name = "targeted_user_name")
    private String name;
    
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
	@Column(name = "deleted", nullable = false  )
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public PendingCertifiedProductEntity getMappedProduct() {
		return mappedProduct;
	}

	public void setMappedProduct(PendingCertifiedProductEntity mappedProduct) {
		this.mappedProduct = mappedProduct;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Long getTargetedUserId() {
		return targetedUserId;
	}

	public void setTargetedUserId(Long targetedUserId) {
		this.targetedUserId = targetedUserId;
	}
	
	
}
