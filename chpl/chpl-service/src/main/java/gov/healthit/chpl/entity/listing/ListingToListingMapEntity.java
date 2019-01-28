package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "listing_to_listing_map")
public class ListingToListingMapEntity {
    private static final long serialVersionUID = -2928065796550375579L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_to_listing_map_id", nullable = false)
    private Long id;

    @Column(name = "parent_listing_id", nullable = false)
    private Long parentId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_listing_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity parent;

    @Column(name = "child_listing_id", nullable = false)
    private Long childId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "child_listing_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity child;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(final Long parentId) {
        this.parentId = parentId;
    }

    public CertifiedProductDetailsEntity getParent() {
        return parent;
    }

    public void setParent(final CertifiedProductDetailsEntity parent) {
        this.parent = parent;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(final Long childId) {
        this.childId = childId;
    }

    public CertifiedProductDetailsEntity getChild() {
        return child;
    }

    public void setChild(final CertifiedProductDetailsEntity child) {
        this.child = child;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
