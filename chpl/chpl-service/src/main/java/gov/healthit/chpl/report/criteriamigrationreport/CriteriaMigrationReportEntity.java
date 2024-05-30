package gov.healthit.chpl.report.criteriamigrationreport;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "criteria_migration_report")
public class CriteriaMigrationReportEntity extends EntityAudit {
    private static final long serialVersionUID = 4878706877789631917L;

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
    @Column(name = "report_name", nullable = false)
    private String reportName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    @Column(name = "criteria_migration_report_id", nullable = false)
    private List<CriteriaMigrationDefinitionEntity> criteriaMigrationDefinitions;

    public CriteriaMigrationReport toDomain() {
        return CriteriaMigrationReport.builder()
                .id(this.id)
                .reportName(this.reportName)
                .criteriaMigrationDefinitions(this.criteriaMigrationDefinitions.stream()
                        .map(e -> e.toDomain())
                        .toList())
                .build();
    }
}
