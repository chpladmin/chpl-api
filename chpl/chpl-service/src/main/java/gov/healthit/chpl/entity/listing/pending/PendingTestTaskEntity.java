package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

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
    private String taskSuccessAverage;

    @Column(name = "task_success_stddev_pct", nullable = false)
    private String taskSuccessStddev;

    @Column(name = "task_path_deviation_observed", nullable = false)
    private String taskPathDeviationObserved;

    @Column(name = "task_path_deviation_optimal", nullable = false)
    private String taskPathDeviationOptimal;

    @Column(name = "task_time_avg_seconds", nullable = false)
    private String taskTimeAvg;

    @Column(name = "task_time_stddev_seconds", nullable = false)
    private String taskTimeStddev;

    @Column(name = "task_time_deviation_observed_avg_seconds", nullable = false)
    private String taskTimeDeviationObservedAvg;

    @Column(name = "task_time_deviation_optimal_avg_seconds", nullable = false)
    private String taskTimeDeviationOptimalAvg;

    @Column(name = "task_errors_pct", nullable = false)
    private String taskErrors;

    @Column(name = "task_errors_stddev_pct", nullable = false)
    private String taskErrorsStddev;

    @Column(name = "task_rating_scale", nullable = false)
    private String taskRatingScale;

    @Column(name = "task_rating", nullable = false)
    private String taskRating;

    @Column(name = "task_rating_stddev", nullable = false)
    private String taskRatingStddev;

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

    public void setId(final Long id) {
        this.id = id;
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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getTaskSuccessAverage() {
        return taskSuccessAverage;
    }

    public void setTaskSuccessAverage(final String taskSuccessAverage) {
        this.taskSuccessAverage = taskSuccessAverage;
    }

    public String getTaskSuccessStddev() {
        return taskSuccessStddev;
    }

    public void setTaskSuccessStddev(final String taskSuccessStddev) {
        this.taskSuccessStddev = taskSuccessStddev;
    }

    public String getTaskPathDeviationObserved() {
        return taskPathDeviationObserved;
    }

    public void setTaskPathDeviationObserved(final String taskPathDeviationObserved) {
        this.taskPathDeviationObserved = taskPathDeviationObserved;
    }

    public String getTaskPathDeviationOptimal() {
        return taskPathDeviationOptimal;
    }

    public void setTaskPathDeviationOptimal(final String taskPathDeviationOptimal) {
        this.taskPathDeviationOptimal = taskPathDeviationOptimal;
    }

    public String getTaskTimeAvg() {
        return taskTimeAvg;
    }

    public void setTaskTimeAvg(final String taskTimeAvg) {
        this.taskTimeAvg = taskTimeAvg;
    }

    public String getTaskTimeStddev() {
        return taskTimeStddev;
    }

    public void setTaskTimeStddev(final String taskTimeStddev) {
        this.taskTimeStddev = taskTimeStddev;
    }

    public String getTaskTimeDeviationObservedAvg() {
        return taskTimeDeviationObservedAvg;
    }

    public void setTaskTimeDeviationObservedAvg(final String taskTimeDeviationObservedAvg) {
        this.taskTimeDeviationObservedAvg = taskTimeDeviationObservedAvg;
    }

    public String getTaskTimeDeviationOptimalAvg() {
        return taskTimeDeviationOptimalAvg;
    }

    public void setTaskTimeDeviationOptimalAvg(final String taskTimeDeviationOptimalAvg) {
        this.taskTimeDeviationOptimalAvg = taskTimeDeviationOptimalAvg;
    }

    public String getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(final String taskErrors) {
        this.taskErrors = taskErrors;
    }

    public String getTaskErrorsStddev() {
        return taskErrorsStddev;
    }

    public void setTaskErrorsStddev(final String taskErrorsStddev) {
        this.taskErrorsStddev = taskErrorsStddev;
    }

    public String getTaskRatingScale() {
        return taskRatingScale;
    }

    public void setTaskRatingScale(final String taskRatingScale) {
        this.taskRatingScale = taskRatingScale;
    }

    public String getTaskRating() {
        return taskRating;
    }

    public void setTaskRating(final String taskRating) {
        this.taskRating = taskRating;
    }

    public String getTaskRatingStddev() {
        return taskRatingStddev;
    }

    public void setTaskRatingStddev(final String taskRatingStddev) {
        this.taskRatingStddev = taskRatingStddev;
    }

    @Override
    public String toString() {
        return "PendingTestTaskEntity [id=" + id + ", uniqueId=" + uniqueId + ", description=" + description
                + ", taskSuccessAverage=" + taskSuccessAverage + ", taskSuccessStddev=" + taskSuccessStddev
                + ", taskPathDeviationObserved=" + taskPathDeviationObserved + ", taskPathDeviationOptimal="
                + taskPathDeviationOptimal + ", taskTimeAvg=" + taskTimeAvg + ", taskTimeStddev=" + taskTimeStddev
                + ", taskTimeDeviationObservedAvg=" + taskTimeDeviationObservedAvg + ", taskTimeDeviationOptimalAvg="
                + taskTimeDeviationOptimalAvg + ", taskErrors=" + taskErrors + ", taskErrorsStddev=" + taskErrorsStddev
                + ", taskRatingScale=" + taskRatingScale + ", taskRating=" + taskRating + ", taskRatingStddev="
                + taskRatingStddev + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedUser=" + lastModifiedUser
                + ", creationDate=" + creationDate + ", deleted=" + deleted + "]";
    }
}
