package gov.healthit.chpl.entity.statistics;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.statistics.CuresListingStatistic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cures_listing_statistics")
public class CuresListingStatisticEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "cures_listing_without_cures_criteria_count", nullable = false)
    private Long curesListingWithoutCuresCriteriaCount;

    @Basic(optional = false)
    @Column(name = "cures_listing_withcures_criteria_count", nullable = false)
    private Long curesListingWithCuresCriteriaCount;

    @Basic(optional = false)
    @Column(name = "non_cures_listing_count", nullable = false)
    private Long nonCuresListingCount;

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

    public CuresListingStatisticEntity(CuresListingStatistic domain) {
        this.id = domain.getId();
        this.curesListingWithoutCuresCriteriaCount = domain.getCuresListingWithoutCuresCriteriaCount();
        this.curesListingWithCuresCriteriaCount = domain.getCuresListingWithCuresCriteriaCount();
        this.nonCuresListingCount = domain.getNonCuresListingCount();
        this.statisticDate = domain.getStatisticDate();
        this.creationDate = domain.getCreationDate();
        this.deleted = domain.getDeleted();
        this.lastModifiedDate = domain.getLastModifiedDate();
        this.lastModifiedUser = domain.getLastModifiedUser();
    }
}
