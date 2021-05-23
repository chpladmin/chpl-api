package gov.healthit.chpl.entity.statistics;

import java.time.LocalDate;
import java.util.Date;

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

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.statistics.ListingToCriterionForCuresAchievementStatisticDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "listing_to_criterion_for_cures_achievement_statistic")
public class ListingToCriterionForCuresAchievementStatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Basic(optional = false)
    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public ListingToCriterionForCuresAchievementStatisticDTO toDto() {
        return ListingToCriterionForCuresAchievementStatisticDTO.builder()
                .id(this.getId())
                .criterion(CertificationCriterionDTO.builder()
                        .id(this.getCertificationCriterionId())
                        .number(this.getCertificationCriterion() != null ? this.getCertificationCriterion().getNumber() : null)
                        .title(this.getCertificationCriterion() != null ? this.getCertificationCriterion().getTitle() : null)
                        .build())
                .listingId(this.getListingId())
                .statisticDate(this.getStatisticDate())
        .build();
    }
}
