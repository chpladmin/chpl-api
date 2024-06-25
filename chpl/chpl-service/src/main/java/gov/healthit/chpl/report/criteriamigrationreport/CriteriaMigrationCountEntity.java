package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "criteria_migration_count")
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
    @Column(name = "criteria_migration_definition_id", nullable = false)
    private Long criteriaMigrationDefinitionId;

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
