package gov.healthit.chpl.surveillance.report.entity;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceCapStatus;
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
@Table(name = "quarterly_report_surveillance_cap_status_map")
public class QuarterlyReportSurveillanceCapStatusMapEntity extends EntityAudit {
    private static final long serialVersionUID = -36017118652182515L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quarterly_report_surveillance_map_id")
    private Long quarterlyReportSurveillanceMapId;

    @Column(name = "surveillance_cap_status_id")
    private Long surveillanceCapStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_cap_status_id", insertable = false, updatable = false)
    private SurveillanceCapStatusEntity surveillanceCapStatus;

    public SurveillanceCapStatus toDomain() {
        return SurveillanceCapStatus.builder()
                .id(surveillanceCapStatusId)
                .name(surveillanceCapStatus.getName())
                .build();
    }
}
