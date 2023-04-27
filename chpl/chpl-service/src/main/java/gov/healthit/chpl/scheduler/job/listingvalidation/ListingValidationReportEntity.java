package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listing_validation_report")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingValidationReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "chpl_product_number", nullable = false)
    private String chplProductNumber;

    @Basic(optional = false)
    @Column(name = "certification_body_id", nullable = false)
    private Long certificationBodyId;

    @Basic(optional = false)
    @Column(name = "product", nullable = false)
    private String product;

    @Basic(optional = false)
    @Column(name = "version", nullable = false)
    private String version;

    @Basic(optional = false)
    @Column(name = "developer", nullable = false)
    private String developer;

    @Basic(optional = false)
    @Column(name = "certification_body", nullable = false)
    private String certificationBody;

    @Basic(optional = false)
    @Column(name = "certification_status_name", nullable = false)
    private String certificationStatusName;

    @Basic(optional = false)
    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Basic(optional = false)
    @Column(name = "report_date", nullable = false)
    private ZonedDateTime reportDate;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(nullable = false, name = "deleted")
    private Boolean deleted;

}
