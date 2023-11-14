package gov.healthit.chpl.attestation.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
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
@Table(name = "attestation_period_developer_exception")
public class AttestationPeriodDeveloperExceptionEntity extends EntityAudit{
    private static final long serialVersionUID = -8276017180526885905L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_period_id")
    private AttestationPeriodEntity period;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private DeveloperEntity developer;

    @Column(name = "exception_end")
    private LocalDate exceptionEnd;

}
