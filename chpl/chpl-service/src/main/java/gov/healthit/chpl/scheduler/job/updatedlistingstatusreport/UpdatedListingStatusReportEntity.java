package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
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
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "report_day", nullable = false)
    private LocalDate reportDay;

    @Column(name = "criteria_require_update_count", nullable = true)
    private Long criteriaRequireUpdateCount;

    @Column(name = "days_updated_early", nullable = true)
    private Long daysUpdatedEarly;

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
                .certifiedProductId(certifiedProductId)
                .reportDay(reportDay)
                .criteriaRequireUpdateCount(criteriaRequireUpdateCount)
                .daysUpdatedEarly(daysUpdatedEarly)
                .chplProductNumber(chplProductNumber)
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
