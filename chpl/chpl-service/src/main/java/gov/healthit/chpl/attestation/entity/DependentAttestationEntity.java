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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "dependent_attestation")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DependentAttestationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_attestation_id", nullable = false, insertable = false, updatable = false)
    private AttestationEntity parentAttestation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_id", nullable = false, insertable = false, updatable = false)
    private AttestationEntity attestation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "when_parent_valid_response_id", nullable = false, insertable = false, updatable = false)
    private AttestationValidResponseEntity whenParentValidResponse;

    @Column(name = "sort_order")
    private Long sortOrder;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;
}
