package gov.healthit.chpl.dto.job;

import java.io.Serializable;

import gov.healthit.chpl.entity.job.JobStatusEntity;
import gov.healthit.chpl.entity.job.JobStatusType;

public class JobStatusDTO implements Serializable {
	private static final long serialVersionUID = -7845596230766438264L;
	private Long id;
	private JobStatusType status;
	private Integer percentComplete;
	
	public JobStatusDTO(){}
	
	public JobStatusDTO(JobStatusEntity entity){		
		this.id = entity.getId();
		this.status = entity.getStatus();
		this.percentComplete = entity.getPercentComplete();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JobStatusType getStatus() {
		return status;
	}

	public void setStatus(JobStatusType status) {
		this.status = status;
	}

	public Integer getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Integer percentComplete) {
		this.percentComplete = percentComplete;
	}

}
