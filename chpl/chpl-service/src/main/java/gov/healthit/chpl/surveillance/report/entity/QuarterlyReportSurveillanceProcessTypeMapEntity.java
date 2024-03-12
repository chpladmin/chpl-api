package gov.healthit.chpl.surveillance.report.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceProcessType;
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
@Table(name = "quarterly_report_surveillance_process_type_map")
public class QuarterlyReportSurveillanceProcessTypeMapEntity extends EntityAudit {
    private static final long serialVersionUID = -3609479886974182515L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quarterly_report_surveillance_map_id")
    private Long quarterlyReportSurveillanceMapId;

    @Column(name = "surveillance_process_type_id")
    private Long surveillanceProcessTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_process_type_id", insertable = false, updatable = false)
    private SurveillanceProcessTypeEntity surveillanceProcessType;

    public SurveillanceProcessType toDomain() {
        SurveillanceProcessType processType = SurveillanceProcessType.builder()
                .id(surveillanceProcessTypeId)
                .name(surveillanceProcessType.getName())
                .build();
        return processType;
    }
}
