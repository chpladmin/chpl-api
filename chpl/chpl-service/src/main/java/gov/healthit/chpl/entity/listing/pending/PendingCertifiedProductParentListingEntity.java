package gov.healthit.chpl.entity.listing.pending;

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

import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_certified_product_parent_listing")
public class PendingCertifiedProductParentListingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_certified_product_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private PendingCertifiedProductEntity mappedProduct;

    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_certified_product_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private CertifiedProductBasicSearchResultEntity parentListing;

    @Column(name = "parent_certified_product_id")
    private Long parentListingId;

    @Column(name = "parent_certified_product_unique_id")
    private String parentListingUniqueId;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public PendingCertifiedProductEntity getMappedProduct() {
        return mappedProduct;
    }

    public void setMappedProduct(final PendingCertifiedProductEntity mappedProduct) {
        this.mappedProduct = mappedProduct;
    }

    public Long getPendingCertifiedProductId() {
        return pendingCertifiedProductId;
    }

    public void setPendingCertifiedProductId(final Long pendingCertifiedProductId) {
        this.pendingCertifiedProductId = pendingCertifiedProductId;
    }

    public CertifiedProductBasicSearchResultEntity getParentListing() {
        return parentListing;
    }

    public void setParentListing(final CertifiedProductBasicSearchResultEntity parentListing) {
        this.parentListing = parentListing;
    }

    public Long getParentListingId() {
        return parentListingId;
    }

    public void setParentListingId(final Long parentListingId) {
        this.parentListingId = parentListingId;
    }

    public String getParentListingUniqueId() {
        return parentListingUniqueId;
    }

    public void setParentListingUniqueId(final String parentListingUniqueId) {
        this.parentListingUniqueId = parentListingUniqueId;
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
}
