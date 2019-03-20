package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * Object mapping for just a few fields
 * from pending_certified_product that we want to pull
 * back as metadata.
 * Read-only.
 * @author kekey
 *
 */
@Entity
@Immutable
@Table(name = "pending_certified_product")
public class PendingCertifiedProductMetadataEntity {

    @Id
    @Column(name = "pending_certified_product_id", nullable = false)
    private Long id;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "unique_id")
    private String uniqueId;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_version")
    private String productVersion;

    @Column(name = "certification_date")
    private Date certificationDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(final Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(final Integer warningCount) {
        this.warningCount = warningCount;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(final Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(final String productVersion) {
        this.productVersion = productVersion;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = certificationDate;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

}
