package gov.healthit.chpl.surveillance.report.entity;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceGroundsForInitiating;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quarterly_report_surveillance_grounds_for_initiating_map")
public class QuarterlyReportSurveillanceGroundsForInitiatingMapEntity extends EntityAudit {
    private static final long serialVersionUID = -360171186974182515L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quarterly_report_surveillance_map_id")
    private Long quarterlyReportSurveillanceMapId;

    @Column(name = "surveillance_grounds_for_initiating_id")
    private Long surveillanceGroundsForInitiatingId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_grounds_for_initiating_id", insertable = false, updatable = false)
    private SurveillanceGroundsForInitiatingEntity surveillanceGroundsForInitiating;

    public SurveillanceGroundsForInitiating toDomain() {
        return SurveillanceGroundsForInitiating.builder()
                .id(surveillanceGroundsForInitiatingId)
                .name(surveillanceGroundsForInitiating.getName())
                .build();
    }
}
