package gov.healthit.chpl.job;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
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
			break;
		default:
			throw new NoJobTypeException();
		}

		result.setJob(job);
		if(Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
			JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
			jobUser.setFirstName(job.getUser().getFirstName());
			jobUser.setId(job.getUser().getId());
			jobUser.setLastName(job.getUser().getLastName());
			jobUser.setSubjectName(job.getUser().getSubjectName());
			//NOTE We can't set the granted authorities here because
			//they come from a JWT not from the user DTO in the database.
			//May need to look into this some more if some type of job needs to make a call with permissions.
			result.setUser(jobUser);
		} else {
			result.setUser(Util.getCurrentUser());
		}
		return result;
	}

	@Lookup
	public MeaningfulUseUploadJob getMeaningfulUseUploadJob(){
		//return (MeaningfulUseUploadJob) context.getBean("meaningfulUseUploadJob");
		//spring will override this method
		//and create a new instance of MeaningfulUseUploadJob
		return null;
	}
}
