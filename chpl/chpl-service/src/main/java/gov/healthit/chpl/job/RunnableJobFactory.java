package gov.healthit.chpl.job;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;

@Component
public class RunnableJobFactory {
	
	public RunnableJob getRunnableJob(JobDTO job) throws NoJobTypeException {
		RunnableJob result = null;
		JobTypeDTO jobType = job.getJobType();
		if(jobType == null || StringUtils.isEmpty(jobType.getName())) {
			throw new NoJobTypeException();
		}
		
		//find the job type enum value
		JobTypeConcept jobTypeConcept = JobTypeConcept.findByName(jobType.getName());
		if(jobTypeConcept == null) {
			throw new NoJobTypeException();
		}
		
		switch(jobTypeConcept) {
		case MUU_UPLOAD:
			result = getMeaningfulUseUploadJob();
			result.setJob(job);
			break;
		default:
			throw new NoJobTypeException();
		}
		return result;
	}
	
	@Lookup
	public MeaningfulUseUploadJob getMeaningfulUseUploadJob(){
		//spring will override this method
		//and create a new instance of MeaningfulUseUploadJob
		return null;
	}
}
