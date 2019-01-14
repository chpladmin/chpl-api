package gov.healthit.chpl.entity.surveillance;

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

import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.util.Util;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_id", nullable = false)
    private Set<PendingSurveillanceRequirementEntity> surveilledRequirements = new HashSet<PendingSurveillanceRequirementEntity>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_id", nullable = false)
    private Set<PendingSurveillanceValidationEntity> validation = new HashSet<PendingSurveillanceValidationEntity>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getCertifiedProductUniqueId() {
        return certifiedProductUniqueId;
    }

    public void setCertifiedProductUniqueId(final String certifiedProductUniqueId) {
        this.certifiedProductUniqueId = certifiedProductUniqueId;
    }

    public Date getStartDate() {
        return Util.getNewDate(startDate);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = Util.getNewDate(startDate);
    }

    public Date getEndDate() {
        return Util.getNewDate(endDate);
    }

    public void setEndDate(final Date endDate) {
        this.endDate = Util.getNewDate(endDate);
    }

    public String getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final String surveillanceType) {
        this.surveillanceType = surveillanceType;
    }

    public Integer getNumRandomizedSites() {
        return numRandomizedSites;
    }

    public void setNumRandomizedSites(final Integer numRandomizedSites) {
        this.numRandomizedSites = numRandomizedSites;
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

    public Set<PendingSurveillanceRequirementEntity> getSurveilledRequirements() {
        return surveilledRequirements;
    }

    public void setSurveilledRequirements(final Set<PendingSurveillanceRequirementEntity> surveilledRequirements) {
        this.surveilledRequirements = surveilledRequirements;
    }

    public CertifiedProductEntity getCertifiedProduct() {
        return certifiedProduct;
    }

    public void setCertifiedProduct(final CertifiedProductEntity certifiedProduct) {
        this.certifiedProduct = certifiedProduct;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public String getSurvFriendlyIdToReplace() {
        return survFriendlyIdToReplace;
    }

    public void setSurvFriendlyIdToReplace(final String survFriendlyIdToReplace) {
        this.survFriendlyIdToReplace = survFriendlyIdToReplace;
    }

    public Set<PendingSurveillanceValidationEntity> getValidation() {
        return validation;
    }

    public void setValidation(final Set<PendingSurveillanceValidationEntity> validation) {
        this.validation = validation;
    }
}
