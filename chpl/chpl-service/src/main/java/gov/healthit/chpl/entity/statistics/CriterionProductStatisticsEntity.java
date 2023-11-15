package gov.healthit.chpl.entity.statistics;

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
import javax.persistence.Transient;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
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
@Table(name = "criterion_product_statistics")
public class CriterionProductStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = -4258273713908999510L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "product_count", nullable = false)
    private Long productCount;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    public CriterionProductStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return CriterionProductStatisticsEntity.class;
    }

}
