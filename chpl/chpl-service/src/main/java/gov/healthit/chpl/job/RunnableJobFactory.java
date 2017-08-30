package gov.healthit.chpl.job;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.dto.JobTypeDTO;

@Component
public class RunnableJobFactory {
	
	public RunnableJob getRunnableJob(JobDTO job) throws NoJobTypeException {
		RunnableJob result = null;
		JobTypeDTO jobType = job.getJobType();
		if(jobType == null || StringUtils.isEmpty(jobType.getName())) {
			throw new NoJobTypeException();
		}
		
		//find the job type enum value
		JobTypeConcept jobTypeConcept = null;
		JobTypeConcept[] availableJobTypes = JobTypeConcept.values();
		for(int i = 0; i < availableJobTypes.length && jobTypeConcept == null; i++) {
			if(availableJobTypes[i].getName().equalsIgnoreCase(jobType.getName())) {
				jobTypeConcept = availableJobTypes[i];
			}
		}
		
		switch(jobTypeConcept) {
		case MUU_UPLOAD:
			result = getMuuJob();
			result.setJob(job);
			break;
		default:
			throw new NoJobTypeException();
		}
		return result;
	}
	
	@Lookup
	public MeaningfulUseUploadJob getMuuJob(){
		//spring will override this method
		//and create a new instance of MeaningfulUseUploadJob
		return null;
	}
}
