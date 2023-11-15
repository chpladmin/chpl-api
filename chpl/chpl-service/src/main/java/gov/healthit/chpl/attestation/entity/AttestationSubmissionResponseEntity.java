package gov.healthit.chpl.attestation.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
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
@Table(name = "attestation_submission_response")
public class AttestationSubmissionResponseEntity extends EntityAudit {
    private static final long serialVersionUID = -4620367900655755890L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "attestation_submission_id", nullable = false)
    private Long attestationSubmissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = true, insertable = true, updatable = true)
    private AllowedResponseEntity response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_item_id", nullable = true, insertable = true, updatable = true)
    private FormItemEntity formItem;

}
