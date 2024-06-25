package gov.healthit.chpl.changerequest.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
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
@Table(name = "change_request_attestation_submission")
public class ChangeRequestAttestationSubmissionEntity extends EntityAudit {
    private static final long serialVersionUID = -7206763442336137951L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, insertable = true,
            updatable = false)
    @Where(clause = " deleted <> true ")
    private ChangeRequestEntity changeRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_period_id")
    private AttestationPeriodEntity attestationPeriod;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_attestation_submission_id")
    private Set<ChangeRequestAttestationSubmissionResponseEntity> responses;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "signature_email", nullable = false)
    private String signatureEmail;

    public ChangeRequestAttestationSubmission toDomain() {
        return ChangeRequestAttestationSubmission.builder()
                .id(id)
                .attestationPeriod(attestationPeriod.toDomain())
                .signature(signature)
                .signatureEmail(signatureEmail)
                .build();
    }
}
