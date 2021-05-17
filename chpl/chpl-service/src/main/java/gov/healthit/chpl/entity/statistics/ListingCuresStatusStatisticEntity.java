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

import gov.healthit.chpl.dto.statistics.ListingCuresStatusStatisticDTO;
import lombok.Data;

@Entity
@Data
@Table(name = "listing_cures_status_statistic")
public class ListingCuresStatusStatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "cures_lisitngs_count", nullable = false)
    private Long curesListingCount;

    @Basic(optional = false)
    @Column(name = "total_listings_count", nullable = false)
    private Long totalListingsCount;

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

    public ListingCuresStatusStatisticDTO toDto() {
        return ListingCuresStatusStatisticDTO.builder()
                .id(this.getId())
                .curesListingCount(this.getCuresListingCount())
                .totalListingCount(this.getTotalListingsCount())
                .statisticDate(this.getStatisticDate())
        .build();
    }
}
