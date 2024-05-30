package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
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
@Table(name = "criteria_migration_definition")
public class CriteriaMigrationCountEntity extends EntityAudit {
    private static final long serialVersionUID = 2391445527765937573L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new SystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Basic(optional = false)
    @Column(name = "original_criterion_count", nullable = false)
    private Integer originalCriterionCount;

    @Basic(optional = false)
    @Column(name = "updated_criterion_count", nullable = false)
    private Integer updatedCriterionCount;

    @Basic(optional = false)
    @Column(name = "original_to_updated_criterion_count", nullable = false)
    private Integer originalToUpdatedCriterionCount;

    public CriteriaMigrationCount toDomain() {
        return CriteriaMigrationCount.builder()
                .id(this.id)
                .reportDate(this.reportDate)
                .originalCriterionCount(this.originalCriterionCount)
                .updatedCriterionCount(this.updatedCriterionCount)
                .originalToUpdatedCriterionCount(this.originalToUpdatedCriterionCount)
                .build();
    }
}
