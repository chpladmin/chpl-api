package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Type;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@SelectBeforeUpdate
@DynamicUpdate
@Entity
@Log4j2
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

    @Column(name = "status")
    @Type(type = "gov.healthit.chpl.upload.listing.PostgresListingUploadStatus",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClassName",
            value = "gov.healthit.chpl.upload.listing.ListingUploadStatus")
    })
    private ListingUploadStatus status;

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

    public ListingUpload toDomain() {
        return ListingUpload.builder()
                .id(this.getId())
                .acb(this.getCertificationBody() != null ? this.getCertificationBody().toDomain() :
                    CertificationBody.builder()
                        .id(this.getCertificationBodyId())
                    .build())
                .chplProductNumber(this.getChplProductNumber())
                .certificationDate(this.getCertificationDate())
                .developer(this.getDeveloperName())
                .product(this.getProductName())
                .version(this.getVersionName())
                .errorCount(this.getErrorCount())
                .warningCount(this.getWarningCount())
                .status(this.getStatus())
                .certifiedProductId(this.getCertifiedProductId())
                .build();
    }

    public ListingUpload toDomainWithRecords() {
        ListingUpload listingUpload = toDomain();
        listingUpload.setRecords(recordsFromString(this.getFileContents()));
        return listingUpload;
    }

    private List<CSVRecord> recordsFromString(String csvRecordStr) {
        List<CSVRecord> records = null;
        try {
            StringReader in = new StringReader(csvRecordStr);
            CSVParser csvParser = CSVFormat.EXCEL.parse(in);
            records = csvParser.getRecords();
        } catch (IOException ex) {
            LOGGER.error("Could not convert the string: '" + csvRecordStr + "' to CSV records.");
        }
        return records;
    }
}
