package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;
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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "test_task")
public class TestTaskEntity implements Serializable {
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

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Float getTaskSuccessAverage() {
        return taskSuccessAverage;
    }

    public void setTaskSuccessAverage(final Float taskSuccessAverage) {
        this.taskSuccessAverage = taskSuccessAverage;
    }

    public Float getTaskSuccessStddev() {
        return taskSuccessStddev;
    }

    public void setTaskSuccessStddev(final Float taskSuccessStddev) {
        this.taskSuccessStddev = taskSuccessStddev;
    }

    public Integer getTaskPathDeviationObserved() {
        return taskPathDeviationObserved;
    }

    public void setTaskPathDeviationObserved(final Integer taskPathDeviationObserved) {
        this.taskPathDeviationObserved = taskPathDeviationObserved;
    }

    public Integer getTaskPathDeviationOptimal() {
        return taskPathDeviationOptimal;
    }

    public void setTaskPathDeviationOptimal(final Integer taskPathDeviationOptimal) {
        this.taskPathDeviationOptimal = taskPathDeviationOptimal;
    }

    public Long getTaskTimeAvg() {
        return taskTimeAvg;
    }

    public void setTaskTimeAvg(final Long taskTimeAvg) {
        this.taskTimeAvg = taskTimeAvg;
    }

    public Integer getTaskTimeStddev() {
        return taskTimeStddev;
    }

    public void setTaskTimeStddev(final Integer taskTimeStddev) {
        this.taskTimeStddev = taskTimeStddev;
    }

    public Integer getTaskTimeDeviationObservedAvg() {
        return taskTimeDeviationObservedAvg;
    }

    public void setTaskTimeDeviationObservedAvg(final Integer taskTimeDeviationObservedAvg) {
        this.taskTimeDeviationObservedAvg = taskTimeDeviationObservedAvg;
    }

    public Integer getTaskTimeDeviationOptimalAvg() {
        return taskTimeDeviationOptimalAvg;
    }

    public void setTaskTimeDeviationOptimalAvg(final Integer taskTimeDeviationOptimalAvg) {
        this.taskTimeDeviationOptimalAvg = taskTimeDeviationOptimalAvg;
    }

    public Float getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(final Float taskErrors) {
        this.taskErrors = taskErrors;
    }

    public Float getTaskErrorsStddev() {
        return taskErrorsStddev;
    }

    public void setTaskErrorsStddev(final Float taskErrorsStddev) {
        this.taskErrorsStddev = taskErrorsStddev;
    }

    public String getTaskRatingScale() {
        return taskRatingScale;
    }

    public void setTaskRatingScale(final String taskRatingScale) {
        this.taskRatingScale = taskRatingScale;
    }

    public Float getTaskRating() {
        return taskRating;
    }

    public void setTaskRating(final Float taskRating) {
        this.taskRating = taskRating;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Float getTaskRatingStddev() {
        return taskRatingStddev;
    }

    public void setTaskRatingStddev(final Float taskRatingStddev) {
        this.taskRatingStddev = taskRatingStddev;
    }

    public Set<TestTaskParticipantMapEntity> getTestParticipants() {
        return testParticipants;
    }

    public void setTestParticipants(final Set<TestTaskParticipantMapEntity> testParticipants) {
        this.testParticipants = testParticipants;
    }
}
