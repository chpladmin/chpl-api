package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

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
@Table(name = "updated_criteria_status_report")
public class UpdatedCriteriaStatusReportEntity extends EntityAudit {
    private static final long serialVersionUID = 6536667108192254975L;

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
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = false)
    @Column(name = "report_day", nullable = false)
    private LocalDate reportDay;

    @Column(name = "listings_with_criterion_count", nullable = true)
    private Integer listingsWithCriterionCount;

    @Column(name = "fully_up_to_date_count", nullable = true)
    private Integer fullyUpToDateCount;

    @Column(name = "functionalities_tested_up_to_date_count", nullable = true)
    private Integer functionalitiesTestedUpToDateCount;

    @Column(name = "standards_up_to_date_count", nullable = true)
    private Integer standardsUpToDateCount;

    @Column(name = "code_sets_up_to_date_count", nullable = true)
    private Integer codeSetsUpToDateCount;

    public UpdatedCriteriaStatusReport toDomain() {
        return UpdatedCriteriaStatusReport.builder()
                .id(id)
                .certificationCriterionId(certificationCriterionId)
                .reportDay(reportDay)
                .listingsWithCriterionCount(listingsWithCriterionCount)
                .fullyUpToDateCount(fullyUpToDateCount)
                .functionalitiesTestedUpToDateCount(functionalitiesTestedUpToDateCount)
                .standardsUpToDateCount(standardsUpToDateCount)
                .codeSetsUpToDateCount(codeSetsUpToDateCount)
                .build();
    }
}
