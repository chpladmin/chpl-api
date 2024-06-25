package gov.healthit.chpl.entity.listing;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "cqm_result_criteria")
public class CQMResultCriteriaEntity extends EntityAudit {
    private static final long serialVersionUID = -6371762888143424213L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "cqm_result_criteria_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "cqm_result_id", nullable = false)
    private Long cqmResultId;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private CertificationCriterionEntity certCriteria;

    public CQMResultCertification toDomain() {
        return CQMResultCertification.builder()
                .id(this.getId())
                .certificationId(this.getCertificationCriterionId())
                .criterion(this.getCertCriteria() != null ? this.getCertCriteria().toDomain() : null)
                .certificationNumber(this.getCertCriteria() != null ? this.getCertCriteria().getNumber() : null)
                .build();
    }
}
