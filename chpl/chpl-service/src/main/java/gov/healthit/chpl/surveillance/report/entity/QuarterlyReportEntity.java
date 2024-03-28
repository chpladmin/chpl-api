package gov.healthit.chpl.surveillance.report.entity;

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

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
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
@Table(name = "quarterly_report")
public class QuarterlyReportEntity extends EntityAudit {
    private static final long serialVersionUID = -7014157516604273621L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = false, updatable = false)
    private CertificationBodyEntity acb;

    @Column(name = "year")
    private Integer year;

    @Column(name = "quarter_id")
    private Long quarterId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", insertable = false, updatable = false)
    private QuarterEntity quarter;

    @Column(name = "activities_and_outcomes_summary")
    private String activitiesOutcomesSummary;

    @Column(name = "reactive_surveillance_summary")
    private String reactiveSurveillanceSummary;

    @Column(name = "prioritized_element_summary")
    private String prioritizedElementSummary;

    @Column(name = "disclosure_requirements_summary")
    private String disclosureRequirementsSummary;

    public QuarterlyReport toDomain() {
        CertificationBody reportAcb = null;
        if (this.getAcb() != null) {
            reportAcb = this.getAcb().toDomain();
        } else if (this.getCertificationBodyId() != null) {
            reportAcb = CertificationBody.builder()
                    .id(this.getCertificationBodyId())
                    .build();
        }
        return QuarterlyReport.builder()
                .acb(reportAcb)
                .disclosureRequirementsSummary(this.getDisclosureRequirementsSummary())
                .endDay(LocalDate.of(getYear(), this.getQuarter().getQuarterEndMonth(), this.getQuarter().getQuarterEndDay()))
                .id(this.getId())
                .prioritizedElementSummary(this.getPrioritizedElementSummary())
                .quarter(this.getQuarter().toDomain().getName())
                .reactiveSurveillanceSummary(this.getReactiveSurveillanceSummary())
                .startDay(LocalDate.of(getYear(), this.getQuarter().getQuarterBeginMonth(), this.getQuarter().getQuarterBeginDay()))
                .surveillanceActivitiesAndOutcomes(this.getActivitiesOutcomesSummary())
                .year(this.getYear())
                .build();

    }
}
