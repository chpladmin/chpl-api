package gov.healthit.chpl.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
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
@Table(name = "test_task")
public class TestTaskEntity extends EntityAudit {
    private static final long serialVersionUID = -6364783003138741063L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_task_id", nullable = false)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "task_success_avg_pct", nullable = false)
    private Float taskSuccessAverage;

    @Column(name = "task_success_stddev_pct", nullable = false)
    private Float taskSuccessStddev;

    @Column(name = "task_path_deviation_observed", nullable = false)
    private Integer taskPathDeviationObserved;

    @Column(name = "task_path_deviation_optimal", nullable = false)
    private Integer taskPathDeviationOptimal;

    @Column(name = "task_time_avg_seconds", nullable = false)
    private Long taskTimeAvg;

    @Column(name = "task_time_stddev_seconds", nullable = false)
    private Integer taskTimeStddev;

    @Column(name = "task_time_deviation_observed_avg_seconds", nullable = false)
    private Integer taskTimeDeviationObservedAvg;

    @Column(name = "task_time_deviation_optimal_avg_seconds", nullable = false)
    private Integer taskTimeDeviationOptimalAvg;

    @Column(name = "task_errors_pct", nullable = false)
    private Float taskErrors;

    @Column(name = "task_errors_stddev_pct", nullable = false)
    private Float taskErrorsStddev;

    @Column(name = "task_rating_scale", nullable = false)
    private String taskRatingScale;

    @Column(name = "task_rating", nullable = false)
    private Float taskRating;

    @Column(name = "task_rating_stddev", nullable = false)
    private Float taskRatingStddev;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "testTaskId")
    @Basic(optional = false)
    @Column(name = "test_task_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<TestTaskParticipantMapEntity> testParticipants = new HashSet<TestTaskParticipantMapEntity>();

}
