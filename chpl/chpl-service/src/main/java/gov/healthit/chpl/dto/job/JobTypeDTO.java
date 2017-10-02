package gov.healthit.chpl.dto.job;

import java.io.Serializable;

import gov.healthit.chpl.entity.job.JobTypeEntity;

public class JobTypeDTO implements Serializable {
    private static final long serialVersionUID = -7845596230766088264L;
    private Long id;
    private String name;
    private String description;
    private String successMessage;

    public JobTypeDTO() {
    }

    public JobTypeDTO(JobTypeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.successMessage = entity.getSuccessMessage();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
