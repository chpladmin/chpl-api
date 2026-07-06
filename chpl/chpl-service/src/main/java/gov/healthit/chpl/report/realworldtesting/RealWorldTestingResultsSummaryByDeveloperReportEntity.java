package gov.healthit.chpl.report.realworldtesting;

import java.time.LocalDate;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "real_world_testing_results_summary_by_developer_report")
public class RealWorldTestingResultsSummaryByDeveloperReportEntity extends EntityAudit {
    private static final long serialVersionUID = -1208758504334058121L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "real_world_testing_year")
    private Long realWorldTestingYear;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private DeveloperEntitySimple developer;

    @Column(name = "checked_date")
    private LocalDate checkedDate;

    @Column(name = "checked_count")
    private Long checkedCount;

    @Column(name = "requires_check_count")
    private Long requiresCheckCount;

    public RealWorldTestingSummaryByDeveloperReport toDomain() {
        return RealWorldTestingSummaryByDeveloperReport.builder()
                .id(id)
                .realWorldTestingYear(realWorldTestingYear)
                .developerId(developer != null ? developer.getId() : null)
                .developerName(developer != null ? developer.getName() : null)
                .checkedDate(checkedDate)
                .checkedCount(checkedCount)
                .requiresCheckCount(requiresCheckCount)
                .build();
    }
}
