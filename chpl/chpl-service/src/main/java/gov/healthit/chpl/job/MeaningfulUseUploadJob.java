package gov.healthit.chpl.job;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.JobDTO;

@Component
public class MeaningfulUseUploadJob extends RunnableJob {
	
	public MeaningfulUseUploadJob() {}
	public MeaningfulUseUploadJob(JobDTO job) {
		this.job = job;
	}

	public JobTypeConcept runsJobType() {
		return JobTypeConcept.MUU_UPLOAD;
	}
	
	public void run() {
		//TODO
		//load the muu file (existing code)
		//email the job contact with job type success msg when job complete
	}
}
