package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingTestTaskEntity;

public class PendingTestTaskDTO implements Serializable {
    private static final long serialVersionUID = -2500801204225408178L;
    private Long id;
    private String uniqueId;
    private String description;
    private String taskSuccessAverage;
    private String taskSuccessStddev;
    private String taskPathDeviationObserved;
    private String taskPathDeviationOptimal;
    private String taskTimeAvg;
    private String taskTimeStddev;
    private String taskTimeDeviationObservedAvg;
    private String taskTimeDeviationOptimalAvg;
    private String taskErrors;
    private String taskErrorsStddev;
    private String taskRatingScale;
    private String taskRating;
    private String taskRatingStddev;

    public PendingTestTaskDTO() {
    }

    public PendingTestTaskDTO(final PendingTestTaskEntity entity) {
        this();
        this.setId(entity.getId());
        this.uniqueId = entity.getUniqueId();
        this.description = entity.getDescription();
        this.taskSuccessAverage = entity.getTaskSuccessAverage();
        this.taskSuccessStddev = entity.getTaskSuccessStddev();
        this.taskPathDeviationObserved = entity.getTaskPathDeviationObserved();
        this.taskPathDeviationOptimal = entity.getTaskPathDeviationOptimal();
        this.taskTimeAvg = entity.getTaskTimeAvg();
        this.taskTimeStddev = entity.getTaskTimeStddev();
        this.taskTimeDeviationObservedAvg = entity.getTaskTimeDeviationObservedAvg();
        this.taskTimeDeviationOptimalAvg = entity.getTaskTimeDeviationOptimalAvg();
        this.taskErrors = entity.getTaskErrors();
        this.taskErrorsStddev = entity.getTaskErrorsStddev();
        this.taskRatingScale = entity.getTaskRatingScale();
        this.taskRating = entity.getTaskRating();
        this.taskRatingStddev = entity.getTaskRatingStddev();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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
        return "PendingTestTaskDTO [id=" + id + ", uniqueId=" + uniqueId + ", description=" + description
                + ", taskSuccessAverage=" + taskSuccessAverage + ", taskSuccessStddev=" + taskSuccessStddev
                + ", taskPathDeviationObserved=" + taskPathDeviationObserved + ", taskPathDeviationOptimal="
                + taskPathDeviationOptimal + ", taskTimeAvg=" + taskTimeAvg + ", taskTimeStddev=" + taskTimeStddev
                + ", taskTimeDeviationObservedAvg=" + taskTimeDeviationObservedAvg + ", taskTimeDeviationOptimalAvg="
                + taskTimeDeviationOptimalAvg + ", taskErrors=" + taskErrors + ", taskErrorsStddev=" + taskErrorsStddev
                + ", taskRatingScale=" + taskRatingScale + ", taskRating=" + taskRating + ", taskRatingStddev="
                + taskRatingStddev + "]";
    }
}
