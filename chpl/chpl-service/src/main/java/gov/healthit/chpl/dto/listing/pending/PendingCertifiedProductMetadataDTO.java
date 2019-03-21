package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductMetadataEntity;

/**
 * Pending Certified Product Metadata DTO.
 * @author kekey
 *
 */
public class PendingCertifiedProductMetadataDTO implements Serializable {
    private static final long serialVersionUID = -574271505031201666L;
    private Long id;
    private Integer errorCount;
    private Integer warningCount;
    private Long developerId;
    private Long productId;
    private Long productVersionId;
    private Long acbId;
    private String uniqueId;
    private String developerName;
    private String productName;
    private String productVersion;
    private Date certificationDate;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductMetadataDTO() {
    }

    /**
     * Construct with entity.
     * @param entity the entity
     */
    public PendingCertifiedProductMetadataDTO(final PendingCertifiedProductMetadataEntity entity) {
        this();
        this.id = entity.getId();
        this.errorCount = entity.getErrorCount();
        this.warningCount = entity.getWarningCount();
        this.developerId = entity.getDeveloperId();
        this.productId = entity.getProductId();
        this.productVersionId = entity.getProductVersionId();
        this.acbId = entity.getCertificationBodyId();
        this.uniqueId = entity.getUniqueId();
        this.developerName = entity.getDeveloperName();
        this.productName = entity.getProductName();
        this.productVersion = entity.getProductVersion();
        this.certificationDate = entity.getCertificationDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
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

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = certificationDate;
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(Long acbId) {
        this.acbId = acbId;
    }

}
