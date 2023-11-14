package gov.healthit.chpl.changerequest.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
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
@Table(name = "change_request_attestation_submission_response")
public class ChangeRequestAttestationSubmissionResponseEntity extends EntityAudit {
    private static final long serialVersionUID = -8169486432305934142L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "change_request_attestation_submission_id", insertable = true, nullable = false)
    private Long changeRequestAttestationSubmissionId;

    @OneToOne()
    @JoinColumn(name = "form_item_id", insertable = true, updatable = false)
    private FormItemEntity formItem;

    @OneToOne()
    @JoinColumn(name = "response_id", insertable = true, updatable = true)
    private AllowedResponseEntity response;

    @Column(name = "response_message")
    private String responseMessage;

}
