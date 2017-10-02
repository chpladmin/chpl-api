package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pending_test_task")
public class PendingTestTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_test_task_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "test_task_unique_id", nullable = false)
    private String uniqueId;

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

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getTaskSuccessAverage() {
        return taskSuccessAverage;
    }

    public void setTaskSuccessAverage(Float taskSuccessAverage) {
        this.taskSuccessAverage = taskSuccessAverage;
    }

    public Float getTaskSuccessStddev() {
        return taskSuccessStddev;
    }

    public void setTaskSuccessStddev(Float taskSuccessStddev) {
        this.taskSuccessStddev = taskSuccessStddev;
    }

    public Integer getTaskPathDeviationObserved() {
        return taskPathDeviationObserved;
    }

    public void setTaskPathDeviationObserved(Integer taskPathDeviationObserved) {
        this.taskPathDeviationObserved = taskPathDeviationObserved;
    }

    public Integer getTaskPathDeviationOptimal() {
        return taskPathDeviationOptimal;
    }

    public void setTaskPathDeviationOptimal(Integer taskPathDeviationOptimal) {
        this.taskPathDeviationOptimal = taskPathDeviationOptimal;
    }

    public Long getTaskTimeAvg() {
        return taskTimeAvg;
    }

    public void setTaskTimeAvg(Long taskTimeAvg) {
        this.taskTimeAvg = taskTimeAvg;
    }

    public Integer getTaskTimeStddev() {
        return taskTimeStddev;
    }

    public void setTaskTimeStddev(Integer taskTimeStddev) {
        this.taskTimeStddev = taskTimeStddev;
    }

    public Integer getTaskTimeDeviationObservedAvg() {
        return taskTimeDeviationObservedAvg;
    }

    public void setTaskTimeDeviationObservedAvg(Integer taskTimeDeviationObservedAvg) {
        this.taskTimeDeviationObservedAvg = taskTimeDeviationObservedAvg;
    }

    public Integer getTaskTimeDeviationOptimalAvg() {
        return taskTimeDeviationOptimalAvg;
    }

    public void setTaskTimeDeviationOptimalAvg(Integer taskTimeDeviationOptimalAvg) {
        this.taskTimeDeviationOptimalAvg = taskTimeDeviationOptimalAvg;
    }

    public Float getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(Float taskErrors) {
        this.taskErrors = taskErrors;
    }

    public Float getTaskErrorsStddev() {
        return taskErrorsStddev;
    }

    public void setTaskErrorsStddev(Float taskErrorsStddev) {
        this.taskErrorsStddev = taskErrorsStddev;
    }

    public String getTaskRatingScale() {
        return taskRatingScale;
    }

    public void setTaskRatingScale(String taskRatingScale) {
        this.taskRatingScale = taskRatingScale;
    }

    public Float getTaskRating() {
        return taskRating;
    }

    public void setTaskRating(Float taskRating) {
        this.taskRating = taskRating;
    }

    public Float getTaskRatingStddev() {
        return taskRatingStddev;
    }

    public void setTaskRatingStddev(Float taskRatingStddev) {
        this.taskRatingStddev = taskRatingStddev;
    }
}
