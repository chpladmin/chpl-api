package gov.healthit.chpl.scheduler.job.listingvalidation;

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

@Entity
@Table(name = "listing_validation_report")
@Data
@Builder
@AllArgsConstructor
public class ListingValidationReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "chpl_product_number", nullable = false)
    private String chplProductNumber;

    @Basic(optional = false)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Basic(optional = false)
    @Column(name = "certification_status_name", nullable = false)
    private String certificationStatusName;

    @Basic(optional = false)
    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Basic(optional = false)
    @Column(name = "report_date", nullable = false)
    private Date reportDate;

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
