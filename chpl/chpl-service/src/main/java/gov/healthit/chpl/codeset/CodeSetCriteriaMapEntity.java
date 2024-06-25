package gov.healthit.chpl.codeset;

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
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "code_set_criteria_map")
public class CodeSetCriteriaMapEntity extends EntityAudit {
    private static final long serialVersionUID = 7619361802656158566L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "code_set_id")
    private Long codeSetId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "code_set_id", insertable = false, updatable = false)
    private CodeSetEntity codeSet;

    public CodeSetCriteriaMap toDomain() {
        return CodeSetCriteriaMap.builder()
                .id(id)
                .criterion(criterion.toDomain())
                .codeSet(codeSet.toDomainWithCriteria())
                .build();
    }

}
