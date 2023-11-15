package gov.healthit.chpl.entity.statistics;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
import gov.healthit.chpl.entity.EntityAudit;
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
