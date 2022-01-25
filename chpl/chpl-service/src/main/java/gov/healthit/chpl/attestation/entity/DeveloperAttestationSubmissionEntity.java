package gov.healthit.chpl.attestation.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.developer.DeveloperEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "developer_attestation_submission")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperAttestationSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne()
    @JoinColumn(name = "developer_id")
    private DeveloperEntity developer;

    @OneToOne()
    @JoinColumn(name = "attestation_period_id")
    private AttestationPeriodEntity period;

    @OneToMany()
    @JoinColumn(name = "developer_attestation_submission_id")
    private Set<DeveloperAttestationResponseEntity> responses;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

}
