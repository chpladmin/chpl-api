package gov.healthit.chpl.attestation.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "attestation_submission")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttestationSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "developer_id", nullable = false)
    private Long developerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_period_id", nullable = true, insertable = false, updatable = false)
    private AttestationPeriodEntity attestationPeriod;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "signature_email", nullable = false)
    private String signatureEmail;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public AttestationSubmission toDomain() {
        return AttestationSubmission.builder()
                .id(id)
                .attestationPeriod(attestationPeriod.toDomain())
                .signature(signature)
                .signatureEmail(signatureEmail)
                .build();
    }
}
