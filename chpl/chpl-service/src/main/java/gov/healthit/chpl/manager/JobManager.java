package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.JobDTO;

public interface JobManager {
	public JobDTO getJobById(Long id);
	public List<JobDTO> getAllJobs();
	public List<JobDTO> getAllRunningJobs();
	public List<JobDTO> getJobsForUser(ContactDTO contact) throws EntityRetrievalException;
	public JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException;
	
	public void start(JobDTO job);
}
