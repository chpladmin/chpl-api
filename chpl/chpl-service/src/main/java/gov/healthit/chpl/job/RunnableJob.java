package gov.healthit.chpl.job;

import gov.healthit.chpl.dto.JobDTO;

public abstract class RunnableJob implements Runnable {
	protected JobDTO job;

	public JobDTO getJob() {
		return job;
	}
	public void setJob(JobDTO job) {
		this.job = job;
	}
	
	public abstract void run();
}
