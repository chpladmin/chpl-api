package gov.healthit.chpl.attestation.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "attestation")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany
    @JoinTable(name = "attestation_form",
            joinColumns = @JoinColumn(name = "attestation_id"),
            inverseJoinColumns = @JoinColumn(name = "attestation_valid_response_id"))
    private Set<AttestationValidResponseEntity> validResponses;

    @OneToOne
    @JoinTable(name = "attestation_form",
            joinColumns = @JoinColumn(name = "attestation_period_id"))
    private AttestationPeriodEntity attestationPeriod;

    @OneToMany()
    @JoinColumn(name = "parent_attestation_id")
    private Set<DependentAttestationEntity> dependentAttestations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_condition_id", nullable = false, insertable = false, updatable = false)
    private AttestationConditionEntity condition;

    @Column(name = "description")
    private String description;

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
