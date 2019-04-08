package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMetadataDTO;

/**
 * Pending Certified Product Metadata domain object.
 * @author kekey
 *
 */
public class PendingCertifiedProductMetadata implements Serializable {
    private static final long serialVersionUID = -6034612936287258033L;
    private Long id;
    private String chplProductNumber;
    private Developer developer;
    private Product product;
    private ProductVersion version;
    private Long certificationDate;
    private Integer errorCount;
    private Integer warningCount;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductMetadata() {
    }

    /**
     * Constructor from DTO.
     * @param dto the DTO
     */
    public PendingCertifiedProductMetadata(final PendingCertifiedProductMetadataDTO dto) {
        this.id = dto.getId();
        this.chplProductNumber = dto.getUniqueId();
        if (dto.getDeveloperId() != null || !StringUtils.isEmpty(dto.getDeveloperName())) {
            Developer dev = new Developer();
            dev.setDeveloperId(dto.getDeveloperId());
            dev.setName(dto.getDeveloperName());
            this.developer = dev;
        }
        if (dto.getProductId() != null || !StringUtils.isEmpty(dto.getProductName())) {
            Product prod = new Product();
            prod.setProductId(dto.getProductId());
            prod.setName(dto.getProductName());
            this.product = prod;
        }
        if (dto.getProductVersionId() != null || !StringUtils.isEmpty(dto.getProductVersion())) {
            ProductVersion ver = new ProductVersion();
            ver.setVersionId(dto.getProductVersionId());
            ver.setVersion(dto.getProductVersion());
            this.version = ver;
        }
        if (dto.getCertificationDate() != null) {
            this.certificationDate = dto.getCertificationDate().getTime();
        }
        this.errorCount = dto.getErrorCount();
        this.warningCount = dto.getWarningCount();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(final ProductVersion version) {
        this.version = version;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Long certificationDate) {
        this.certificationDate = certificationDate;
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
}
