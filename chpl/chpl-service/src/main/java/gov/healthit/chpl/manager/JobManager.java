package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;

public interface JobManager {
    JobDTO getJobById(Long id);

    List<JobDTO> getAllJobs();

    List<JobTypeDTO> getAllJobTypes();

    List<JobDTO> getJobsForUser(UserDTO user) throws EntityRetrievalException;

    JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException;

    boolean start(JobDTO job) throws EntityRetrievalException;
}
