package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.ListingToListingMapEntity;

public class ListingToListingMapDTO implements Serializable {
	private static final long serialVersionUID = -1863384989196377585L;
	private Long id;
	private Long parentId;
	private Long childId;
	private CertifiedProductDetailsDTO parent;
	private CertifiedProductDetailsDTO child;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;

	public ListingToListingMapDTO(){
	}
	public ListingToListingMapDTO(ListingToListingMapEntity entity){
		this();

		this.id = entity.getId();
		this.parentId = entity.getParentId();
		this.childId = entity.getChildId();
		this.parent = new CertifiedProductDetailsDTO(entity.getParent());
		this.child = new CertifiedProductDetailsDTO(entity.getChild());

		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}

	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public Long getChildId() {
		return childId;
	}
	public void setChildId(Long childId) {
		this.childId = childId;
	}
	public CertifiedProductDetailsDTO getParent() {
		return parent;
	}
	public void setParent(CertifiedProductDetailsDTO parent) {
		this.parent = parent;
	}
	public CertifiedProductDetailsDTO getChild() {
		return child;
	}
	public void setChild(CertifiedProductDetailsDTO child) {
		this.child = child;
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
}
