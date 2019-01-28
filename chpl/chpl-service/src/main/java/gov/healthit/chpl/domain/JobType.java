package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.job.JobTypeDTO;

public class JobType implements Serializable{
    private static final long serialVersionUID = 8306081196933120077L;

    private Long id;
    private String name;
    private String description;

    public JobType() {
    }

    public JobType(JobTypeDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.description = dto.getDescription();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
