package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMetadataDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
    private boolean processing;

    public PendingCertifiedProductMetadata(PendingCertifiedProductMetadataDTO dto) {
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
        this.processing = dto.isProcessing();
    }
}
