package gov.healthit.chpl.attestation.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.DateUtil;
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
@Table(name = "attestation_submission")
public class AttestationSubmissionEntity extends EntityAudit{
    private static final long serialVersionUID = -7242508236937180150L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "developer_id", nullable = false)
    private Long developerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_period_id", nullable = true, insertable = true, updatable = false)
    private AttestationPeriodEntity attestationPeriod;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_submission_id", nullable = true, insertable = false, updatable = false)
    private List<AttestationSubmissionResponseEntity> responses;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "signature_email", nullable = false)
    private String signatureEmail;

    public AttestationSubmission toDomain() {
        return AttestationSubmission.builder()
                .id(id)
                .developerId(developerId)
                .attestationPeriod(attestationPeriod.toDomain())
                .signature(signature)
                .signatureEmail(signatureEmail)
                .datePublished(DateUtil.toLocalDate(getLastModifiedDate().getTime()))
                .build();
    }
}
