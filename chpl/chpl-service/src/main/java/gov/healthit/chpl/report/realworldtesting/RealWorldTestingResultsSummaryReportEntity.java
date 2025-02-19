package gov.healthit.chpl.report.realworldtesting;

import java.time.LocalDate;

import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "real_world_testing_results_summary_report")
public class RealWorldTestingResultsSummaryReportEntity extends EntityAudit {
    private static final long serialVersionUID = 4976557989831765742L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "real_world_testing_year")
    private Long realWorldTestingYear;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id")
    private CertificationBodyEntity certificationBody;

    @Column(name = "checked_date")
    private LocalDate checkedDate;

    @Column(name = "checked_count")
    private Long checkedCount;

    @Column(name = "requires_check_count")
    private Long requiresCheckCount;

    public RealWorldTestingSummaryReport toDomain() {
        return RealWorldTestingSummaryReport.builder()
                .id(id)
                .realWorldTestingYear(realWorldTestingYear)
                .certificationBody(certificationBody.toDomain())
                .checkedDate(checkedDate)
                .checkedCount(checkedCount)
                .requiresCheckCount(requiresCheckCount)
                .build();
    }

}
