package gov.healthit.chpl.changerequest.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
