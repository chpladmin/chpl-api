package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobMessageDTO;

public class Job implements Serializable {
    private static final long serialVersionUID = 5428087924037963866L;
    private Long id;
    private JobType type;
    private User user;
    private JobStatus status;
    private Long startTime;
    private Long endTime;
    private List<String> messages;

    public Job() {
        messages = new ArrayList<String>();
    }

    public Job(JobDTO dto) {
        this();
        this.id = dto.getId();
        this.type = new JobType(dto.getJobType());
        this.user = new User(dto.getUser());
        if (dto.getStatus() != null) {
            this.status = new JobStatus(dto.getStatus());
        }
        this.startTime = dto.getStartTime() == null ? null : dto.getStartTime().getTime();
        this.endTime = dto.getEndTime() == null ? null : dto.getEndTime().getTime();
        if (dto.getMessages() != null) {
            for (JobMessageDTO message : dto.getMessages()) {
                this.messages.add(message.getMessage());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public JobType getType() {
        return type;
    }

    public void setType(final JobType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(final Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(final Long endTime) {
        this.endTime = endTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(final JobStatus status) {
        this.status = status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(final List<String> messages) {
        this.messages = messages;
    }
}
