package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@DynamicUpdate
@Table(name = "attestation_checkin_report")
public class AttestationCheckinReportEntity extends EntityAudit {
    private static final long serialVersionUID = 457232700018415552L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "developer_name")
    private String developerName;

    @Column(name = "developer_code")
    private String developerCode;

    @Column(name = "developer_id")
    private Long developerId;

    @Column(name = "submitted_datetime")
    private LocalDateTime submittedDate;

    @Column(name = "published")
    private Boolean published;

    @Column(name = "current_status_name")
    private String currentStatusName;

    @Column(name = "last_status_change_datetime")
    private LocalDateTime lastStatusChangeDate;

    @Column(name = "relevant_acbs")
    private String relevantAcbs;

    @Column(name = "attestation_period")
    private String attestationPeriod;

    @Column(name = "information_blocking_response")
    private String informationBlockingResponse;

    @Column(name = "information_blocking_noncompliant_response")
    private String informationBlockingNoncompliantResponse;

    @Column(name = "assurances_response")
    private String assurancesResponse;

    @Column(name = "assurances_noncompliant_response")
    private String assurancesNoncompliantResponse;

    @Column(name = "communications_response")
    private String communicationsResponse;

    @Column(name = "communications_noncompliant_response")
    private String communicationsNoncompliantResponse;

    @Column(name = "rwt_response")
    private String rwtResponse;

    @Column(name = "rwt_noncompliant_response")
    private String rwtNoncompliantResponse;

    @Column(name = "api_response")
    private String apiResponse;

    @Column(name = "api_noncompliant_response")
    private String apiNoncompliantResponse;

    @Column(name = "signature")
    private String signature;

    @Column(name = "signature_email")
    private String signatureEmail;

    @Column(name = "total_surveillances")
    private Long totalSurveillances;

    @Column(name = "total_surveillance_nonconformities")
    private Long totalSurveillanceNonconformities;

    @Column(name = "open_surveillance_nonconformities")
    private Long openSurveillanceNonconformities;

    @Column(name = "total_direct_review_nonconformities")
    private Long totalDirectReviewNonconformities;

    @Column(name = "open_direct_review_nonconformities")
    private Long openDirectReviewNonconformities;

    @Column(name = "assurances_validation")
    private String assurancesValidation;

    @Column(name = "real_world_testing_validation")
    private String realWorldTestingValidation;

    @Column(name = "api_validation")
    private String apiValidation;

    @Column(name = "warnings")
    private String warnings;

    @Column(name = "attests_g7")
    private Boolean attestsG7;

    @Column(name = "attests_g9")
    private Boolean attestsG9;

    @Column(name = "attests_g10")
    private Boolean attestsG10;

    public CheckInReport toDomain() {
        return CheckInReport.builder()
                .developerName(this.getDeveloperName())
                .developerCode(this.getDeveloperCode())
                .developerId(this.getDeveloperId())
                .submittedDate(this.getSubmittedDate())
                .published(this.getPublished())
                .currentStatusName(this.getCurrentStatusName())
                .lastStatusChangeDate(this.getLastStatusChangeDate())
                .relevantAcbs(this.getRelevantAcbs())
                .attestationPeriod(this.getAttestationPeriod())
                .informationBlockingResponse(this.getInformationBlockingResponse())
                .informationBlockingNoncompliantResponse(this.getInformationBlockingNoncompliantResponse())
                .assurancesResponse(this.getAssurancesResponse())
                .assurancesNoncompliantResponse(this.getAssurancesNoncompliantResponse())
                .communicationsResponse(this.getCommunicationsResponse())
                .communicationsNoncompliantResponse(this.getCommunicationsNoncompliantResponse())
                .rwtResponse(this.getRwtResponse())
                .rwtNoncompliantResponse(this.getRwtNoncompliantResponse())
                .apiResponse(this.getApiResponse())
                .apiNoncompliantResponse(this.getApiNoncompliantResponse())
                .signature(this.getSignature())
                .signatureEmail(this.getSignatureEmail())
                .totalSurveillances(this.getTotalSurveillances())
                .totalSurveillanceNonconformities(this.getTotalSurveillanceNonconformities())
                .openSurveillanceNonconformities(this.getOpenSurveillanceNonconformities())
                .totalDirectReviewNonconformities(this.getTotalDirectReviewNonconformities())
                .openDirectReviewNonconformities(this.getOpenDirectReviewNonconformities())
                .assurancesValidation(this.getAssurancesValidation())
                .realWorldTestingValidation(this.getRealWorldTestingValidation())
                .apiValidation(this.getApiValidation())
                .warnings(this.getWarnings())
                .attestsG7(this.getAttestsG7())
                .attestsG9(this.getAttestsG9())
                .attestsG10(this.getAttestsG10())
                .build();
    }
}
