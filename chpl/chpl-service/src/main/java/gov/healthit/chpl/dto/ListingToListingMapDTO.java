package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.ListingToListingMapEntity;
import gov.healthit.chpl.util.Util;

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

    public ListingToListingMapDTO() {
    }

    public ListingToListingMapDTO(ListingToListingMapEntity entity) {
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

    public void setParentId(final Long parentId) {
        this.parentId = parentId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(final Long childId) {
        this.childId = childId;
    }

    public CertifiedProductDetailsDTO getParent() {
        return parent;
    }

    public void setParent(final CertifiedProductDetailsDTO parent) {
        this.parent = parent;
    }

    public CertifiedProductDetailsDTO getChild() {
        return child;
    }

    public void setChild(final CertifiedProductDetailsDTO child) {
        this.child = child;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}
