package gov.healthit.chpl.codesetdate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Table(name = "code_set_date_criteria_map")
public class CodeSetDateCriteriaMapEntity extends EntityAudit {
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

    @Column(name = "code_set_date_id")
    private Long codeSetDateId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "code_set_date_id", insertable = false, updatable = false)
    private CodeSetDateEntity codeSetDate;

    public CodeSetDateCriteriaMap toDomain() {
        return CodeSetDateCriteriaMap.builder()
                .id(id)
                .criterion(criterion.toDomain())
                .codeSetDate(codeSetDate.toDomainWithCriteria())
                .build();
    }

}
