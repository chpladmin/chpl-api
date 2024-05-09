package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Type;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@SelectBeforeUpdate
@DynamicUpdate
@Entity
@Log4j2
@Table(name = "certified_product_upload")
public class ListingUploadEntity extends EntityAudit {
    private static final long serialVersionUID = 8098143453941187347L;

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
