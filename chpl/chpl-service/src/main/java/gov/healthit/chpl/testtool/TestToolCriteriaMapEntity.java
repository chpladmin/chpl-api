package gov.healthit.chpl.testtool;

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
import gov.healthit.chpl.domain.TestToolCriteriaMap;
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
@Table(name = "test_tool_criteria_map")
public class TestToolCriteriaMapEntity extends EntityAudit {
    private static final long serialVersionUID = 2606890203542230945L;

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
    private CertificationCriterionEntity criteria;

    @Column(name = "test_tool_id")
    private Long testToolId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_tool_id", insertable = false, updatable = false)
    private TestToolEntity testTool;

    public TestToolCriteriaMap toDomain() {
        return TestToolCriteriaMap.builder()
                .id(id)
                .criterion(criteria.toDomain())
                .testTool(testTool.toDomainWithCriteria())
                .build();
    }
}
