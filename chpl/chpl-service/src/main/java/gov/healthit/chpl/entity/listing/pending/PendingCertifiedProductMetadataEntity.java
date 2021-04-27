package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Immutable
@Data
@Table(name = "pending_certified_product")
public class PendingCertifiedProductMetadataEntity {

    @Id
    @Column(name = "pending_certified_product_id", nullable = false)
    private Long id;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "processing")
    private boolean processing;

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

    @Column(name = "deleted")
    private Boolean deleted;
}
