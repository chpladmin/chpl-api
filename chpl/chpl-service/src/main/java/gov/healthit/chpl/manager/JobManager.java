package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface JobManager {
    JobDTO getJobById(Long id);

    List<JobDTO> getAllJobs();

    List<JobTypeDTO> getAllJobTypes();

    List<JobDTO> getJobsForUser(UserDTO user) throws EntityRetrievalException;

    JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException;

    boolean start(JobDTO job) throws EntityRetrievalException;
}
