package gov.healthit.chpl.upload.listing;

import java.time.LocalDate;
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

import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.Data;

@Entity
@Table(name = "certified_product_upload")
@Data
public class ListingUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = false, updatable = false)
    private CertificationBodyEntity certificationBody;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "certification_date")
    private LocalDate certificationDate;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "contents")
    private String fileContents;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}
