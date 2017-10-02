package gov.healthit.chpl.dto.job;

import java.io.Serializable;

import gov.healthit.chpl.entity.job.JobMessageEntity;

public class JobMessageDTO implements Serializable {
    private static final long serialVersionUID = -7845386230766438264L;
    private Long id;
    private String message;

    public JobMessageDTO() {
    }

    public JobMessageDTO(JobMessageEntity entity) {
        this.id = entity.getId();
        this.message = entity.getMessage();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
