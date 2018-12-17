package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.job.JobStatusDTO;

public class JobStatus implements Serializable {
    private static final long serialVersionUID = 5418948955268613278L;

    private String status;
    private Integer percentComplete;

    public JobStatus() {
    }

    public JobStatus(JobStatusDTO dto) {
        this.status = dto.getStatus() != null ? dto.getStatus().toString() : null;
        this.percentComplete = dto.getPercentComplete();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(final Integer percentComplete) {
        this.percentComplete = percentComplete;
    }
}
