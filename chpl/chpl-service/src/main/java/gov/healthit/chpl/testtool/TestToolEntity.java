package gov.healthit.chpl.testtool;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.SQLJoinTableRestriction;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "test_tool")
public class TestToolEntity extends EntityAudit {
    private static final long serialVersionUID = -5376346428073267735L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_tool_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "value")
    private String value;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "test_tool_criteria_map",
        joinColumns = {@JoinColumn(name = "test_tool_id", referencedColumnName = "test_tool_id")},
        inverseJoinColumns = {@JoinColumn(name = "certification_criterion_id", referencedColumnName = "certification_criterion_id")})
    @SQLJoinTableRestriction(value = "deleted <> true")
    private List<CertificationCriterionEntity> criteria;

    public TestTool toDomain() {
        return TestTool.builder()
                .id(id)
                .value(value)
                .startDay(startDay)
                .endDay(endDay)
                .build();
    }

    public TestTool toDomainWithCriteria() {
        return TestTool.builder()
                .id(id)
                .value(value)
                .startDay(startDay)
                .endDay(endDay)
                .criteria(criteria == null ? null : criteria.stream().map(crit -> crit.toDomain()).collect(Collectors.toList()))
                .build();
    }
}
