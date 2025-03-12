package gov.healthit.chpl.testdata;

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
@Table(name = "test_data")
public class TestDataEntity extends EntityAudit {
    private static final long serialVersionUID = -2717638994769357135L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "test_data_criteria_map",
        joinColumns = {@JoinColumn(name = "test_data_id", referencedColumnName = "test_data_id")},
        inverseJoinColumns = {@JoinColumn(name = "certification_criterion_id", referencedColumnName = "certification_criterion_id")})
    @SQLJoinTableRestriction(value = "deleted <> true")
    private List<CertificationCriterionEntity> criteria;

    public TestData toDomain() {
        return TestData.builder()
                .id(id)
                .name(name)
                .build();
    }

    public TestData toDomainWithCriteria() {
        return TestData.builder()
                .id(id)
                .name(name)
                .criteria(criteria == null ? null : criteria.stream().map(crit -> crit.toDomain()).collect(Collectors.toList()))
                .build();
    }
}
