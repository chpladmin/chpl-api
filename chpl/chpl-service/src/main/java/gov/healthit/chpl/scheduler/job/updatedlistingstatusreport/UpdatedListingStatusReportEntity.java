package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;

import gov.healthit.chpl.codeset.CodeSetEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedEntity;
import gov.healthit.chpl.standard.StandardEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "updated_listing_status_report")
public class UpdatedListingStatusReportEntity extends EntityAudit {
    private static final long serialVersionUID = 6345202720550402100L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new SystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "report_day", nullable = false)
    private LocalDate reportDay;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_result_id", insertable = false, updatable = false)
    private CertificationResultDetailsEntity certificationResult;

    @Basic(optional = false)
    @Column(name = "standard_id", nullable = false)
    private Long standardId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", insertable = false, updatable = false)
    private StandardEntity standard;

    @Basic(optional = false)
    @Column(name = "functionality_tested_id", nullable = false)
    private Long functionalityTestedId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_tested_id", insertable = false, updatable = false)
    private FunctionalityTestedEntity functionalityTested;

    @Basic(optional = false)
    @Column(name = "code_set_id", nullable = false)
    private Long codeSetId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "code_set_id", insertable = false, updatable = false)
    private CodeSetEntity codeSet;

    @Basic(optional = false)
    @Column(name = "listing_not_up_to_date_reason_id", nullable = false)
    private Long listingNotUpToDateReasonId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_not_up_to_date_reason_id", insertable = false, updatable = false)
    private ListingNotUpToDateReasonEntity listingNotUpToDateReason;

    @Column(name = "chpl_product_number", nullable = false)
    private String chplProductNumber;

    @Column(name = "product", nullable = false)
    private String product;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "developer", nullable = false)
    private String developer;

    @Column(name = "certification_body", nullable = false)
    private String certificationBody;

    @Column(name = "certification_status_name", nullable = false)
    private String certificationStatus;

    @Column(name = "developer_id", nullable = false)
    private Long developerId;

    @Column(name = "certification_body_id", nullable = false)
    private Long certificationBodyId;

    @Column(name = "certification_status_id", nullable = false)
    private Long certificationStatusId;

    public UpdatedListingStatusReport toDomain() {
        return UpdatedListingStatusReport.builder()
                .id(id)
                .reportDay(reportDay)
                .certifiedProductId(certifiedProductId)
                .chplProductNumber(chplProductNumber)
                .certificationResultId(certificationResultId)
                .certificationCriterion(certificationResult.getCertificationCriterion().toDomain())
                .standard(standard != null ? standard.toDomain() : null)
                .functionalityTested(functionalityTested != null ? functionalityTested.toDomain() : null)
                .codeSet(codeSet != null ? codeSet.toDomain() : null)
                .listingNotUpToDateReason(listingNotUpToDateReason.toDomain())
                .product(product)
                .version(version)
                .developer(developer)
                .certificationBody(certificationBody)
                .certificationStatus(certificationStatus)
                .developerId(developerId)
                .certificationBodyId(certificationBodyId)
                .certificationStatusId(certificationStatusId)
                .build();
    }
}
