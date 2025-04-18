package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "attestation_report")
public class AttestationReportEntity extends EntityAudit {
    private static final long serialVersionUID = -3139285302653689705L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_period_id")
    private AttestationPeriodEntity attestationPeriod;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id")
    private CertificationBodyEntity certificationBody;

    @Column(name = "developer_count")
    private Long developerCount;

    @Column(name = "approved_count")
    private Long approvedCount;

    @Column(name = "pending_acb_action_count")
    private Long pendingAcbActionCount;

    @Column(name = "pending_developer_action_count")
    private Long pendingDeveloperActionCount;

    @Column(name = "no_submission_count")
    private Long noSubmissionCount;

    public AttestationReport toDomain() {
        return AttestationReport.builder()
                .id(id)
                .reportDate(reportDate)
                .attestationPeriod(attestationPeriod.toDomain())
                .certificationBody(certificationBody != null
                        ? certificationBody.toDomain()
                        : CertificationBody.builder().id(0L).name("All ONC-ACBs").build())
                .developerCount(developerCount)
                .approvedCount(approvedCount)
                .pendingAcbActionCount(pendingAcbActionCount)
                .pendingDeveloperActionCount(pendingDeveloperActionCount)
                .noSubmissionCount(noSubmissionCount)
                .build();
    }

}
