package gov.healthit.chpl.job;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.JobDTO;

@Component
public class MeaningfulUseUploadJob extends RunnableJob {
	
	public MeaningfulUseUploadJob() {}
	public MeaningfulUseUploadJob(JobDTO job) {
		this.job = job;
	}
	
	public void run() {
		System.out.println("RUNNING MUU JOB!!!!!");
		//TODO
		//load the muu file (existing code)
		//email the job contact with job type success msg when job complete
	}
}
