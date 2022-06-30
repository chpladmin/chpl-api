package gov.healthit.chpl.changerequest.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "change_request_attestation_response")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRequestAttestationResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "change_request_attestation_submission_id", insertable = true, nullable = false)
    private Long changeRequestAttestationSubmissionId;

    /*
    @OneToOne()
    @JoinColumn(name = "attestation_id", insertable = true, updatable = false)
    private AttestationEntity attestation;

    @OneToOne()
    @JoinColumn(name = "attestation_valid_response_id", insertable = true, updatable = true)
    private ValidResponseEntity validResponse;
    */

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

}
