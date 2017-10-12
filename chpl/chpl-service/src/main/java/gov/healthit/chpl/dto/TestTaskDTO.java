package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.entity.TestTaskEntity;
import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;

public class TestTaskDTO implements Serializable {
    private static final long serialVersionUID = 1595127061190153672L;
    private Long id;
    private String description;
    private Float taskSuccessAverage;
    private Float taskSuccessStddev;
    private Integer taskPathDeviationObserved;
    private Integer taskPathDeviationOptimal;
    private Long taskTimeAvg;
    private Integer taskTimeStddev;
    private Integer taskTimeDeviationObservedAvg;
    private Integer taskTimeDeviationOptimalAvg;
    private Float taskErrors;
    private Float taskErrorsStddev;
    private String taskRatingScale;
    private Float taskRating;
    private Float taskRatingStddev;
    private String pendingUniqueId;
    private List<TestParticipantDTO> participants;

    public TestTaskDTO() {
        participants = new ArrayList<TestParticipantDTO>();
    }

    public TestTaskDTO(TestTaskEntity entity) {
        this();

        if (entity != null) {
            this.id = entity.getId();
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
            if (entity.getTestParticipants() != null) {
                for (TestTaskParticipantMapEntity participantMap : entity.getTestParticipants()) {
                    this.participants.add(new TestParticipantDTO(participantMap.getTestParticipant()));
                }
            }
        }
    }

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

    public String getPendingUniqueId() {
        return pendingUniqueId;
    }

    public void setPendingUniqueId(final String pendingUniqueId) {
        this.pendingUniqueId = pendingUniqueId;
    }

    public Float getTaskRatingStddev() {
        return taskRatingStddev;
    }

    public void setTaskRatingStddev(final Float taskRatingStddev) {
        this.taskRatingStddev = taskRatingStddev;
    }

    public List<TestParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(final List<TestParticipantDTO> participants) {
        this.participants = participants;
    }
}
