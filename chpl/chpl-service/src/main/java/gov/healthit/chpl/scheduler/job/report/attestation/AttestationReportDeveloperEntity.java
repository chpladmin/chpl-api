package gov.healthit.chpl.scheduler.job.report.attestation;

import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
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
@Table(name = "attestation_report_developer")
public class AttestationReportDeveloperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_report_id")
    private AttestationReportEntity attestationReport;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private DeveloperEntity developer;

    @Column(name = "change_request_status_type_id")
    private ChangeRequestStatusTypeEntity changeRequestStatusType;

    public AttestationReportDeveloper toDomain() {
        return AttestationReportDeveloper.builder()
                .id(this.id)
                .attestationReport(this.attestationReport.toDomain())
                .developer(this.developer.toDomain())
                .changeRequestStatusType(this.changeRequestStatusType.toDomain())
                .build();
    }
}
