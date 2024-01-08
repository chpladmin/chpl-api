package gov.healthit.chpl.compliance.surveillance.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
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
@Table(name = "surveillance_basic")
public class SurveillanceBasicEntity extends EntityAudit {
    private static final long serialVersionUID = -1249518324870434198L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "open_nonconformity_count")
    private Integer numOpenNonconformities;

    @Column(name = "closed_nonconformity_count")
    private Integer numClosedNonconformities;

    public SurveillanceBasic buildSurveillanceBasic() {
        return SurveillanceBasic.builder()
                .certifiedProductId(this.getCertifiedProductId())
                .chplProductNumber(this.getChplProductNumber())
                .endDay(this.getEndDate())
                .friendlyId(this.getFriendlyId())
                .id(this.getId())
                .numClosedNonconformities(this.getNumClosedNonconformities())
                .numOpenNonconformities(this.getNumOpenNonconformities())
                .numRandomizedSites(this.getNumRandomizedSites())
                .startDay(this.getStartDate())
                .surveillanceType(this.getSurveillanceType() != null ? this.getSurveillanceType().buildSurveillanceType() : null)
                .surveillanceTypeId(this.getSurveillanceTypeId())
            .build();
    }
}
