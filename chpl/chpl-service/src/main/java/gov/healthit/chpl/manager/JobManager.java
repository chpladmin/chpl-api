package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;

public interface JobManager {
	public JobDTO getJobById(Long id);
	public List<JobDTO> getAllJobs();
	public List<JobTypeDTO> getAllJobTypes();
	public List<JobDTO> getJobsForUser(UserDTO user) throws EntityRetrievalException;
	public JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException;

	public boolean start(JobDTO job) throws EntityRetrievalException;
}
