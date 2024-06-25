package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Basic(optional = false)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    @Column(name = "criteria_migration_report_id", nullable = false)
    private List<CriteriaMigrationDefinitionEntity> criteriaMigrationDefinitions;

    public CriteriaMigrationReport toDomain() {
        return CriteriaMigrationReport.builder()
                .id(this.id)
                .reportName(this.reportName)
                .startDate(this.startDate)
                .criteriaMigrationDefinitions(this.criteriaMigrationDefinitions.stream()
                        .map(e -> e.toDomain())
                        .toList())
                .build();
    }
}
