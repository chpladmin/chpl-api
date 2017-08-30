package gov.healthit.chpl.job;

import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.JobDTO;

public abstract class RunnableJob implements Runnable {
	protected JobDTO job;
	
	/**
	 * Declares what type of job this class can handle
	 * @return
	 */
	public abstract JobTypeConcept runsJobType();
	
	public JobDTO getJob() {
		return job;
	}
	public void setJob(JobDTO job) {
		this.job = job;
	}
	
	public void run() {
	}
}
