package gov.healthit.chpl.entity.statistics;

import java.time.LocalDate;

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

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
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
@Table(name = "criterion_upgraded_from_original_listing_statistic")
public class CriterionUpgradedToCuresFromOriginalListingStatisticEntity extends EntityAudit {
    private static final long serialVersionUID = -4175214624438005611L;

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
    @Column(name = "listing_count", nullable = false)
    private Long listingCount;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Basic(optional = false)
    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    public CriterionUpgradedToCuresFromOriginalListingStatistic toDomain() {
        return CriterionUpgradedToCuresFromOriginalListingStatistic.builder()
                .id(this.getId())
                .curesCriterion(CertificationCriterion.builder()
                        .id(this.getCertificationCriterionId())
                        .number(this.getCertificationCriterion() != null ? this.getCertificationCriterion().getNumber() : null)
                        .title(this.getCertificationCriterion() != null ? this.getCertificationCriterion().getTitle() : null)
                        .build())
                .listingsUpgradedFromOriginalCount(this.getListingCount())
                .statisticDate(this.getStatisticDate())
        .build();
    }
}
