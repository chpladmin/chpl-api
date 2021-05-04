package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductMetadataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
    private boolean processing;

    public PendingCertifiedProductMetadataDTO(PendingCertifiedProductMetadataEntity entity) {
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
        this.processing = entity.isProcessing();
    }
}
