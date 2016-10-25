package gov.healthit.chpl.entity;

import java.io.Serializable;
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
@Table(name = "product_owner_history_map")
public class ProductOwnerEntity implements Serializable {
	private static final long serialVersionUID = -8325348768063869639L;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "id", nullable = false  )
	private Long id;
    
	@Column( name = "vendor_id")
	private Long developerId;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", unique=true, nullable = true, insertable=false, updatable=false)
	private DeveloperEntity developer;
	
	@Column(name = "product_id")
	private Long productId;
	
	@Column(name = "transfer_date")
	private java.sql.Date transferDate;
	
	@Column( name = "creation_date", nullable = false, insertable = false, updatable = false  )
	private Date creationDate;
	
	@Column(name = "deleted", nullable = false  )
	private Boolean deleted;
    
	@Column( name = "last_modified_date", nullable = false, insertable = false, updatable = false  )
	private Date lastModifiedDate;
	
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public java.sql.Date getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(java.sql.Date transferDate) {
		this.transferDate = transferDate;
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

	public DeveloperEntity getDeveloper() {
		return developer;
	}

	public void setDeveloper(DeveloperEntity developer) {
		this.developer = developer;
	}
	
}
