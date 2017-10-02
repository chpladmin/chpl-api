package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobStatusDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.job.JobStatusType;

public interface JobDAO {

    JobDTO create(JobDTO dto) throws EntityCreationException;

    void markStarted(JobDTO dto) throws EntityRetrievalException;

    JobStatusDTO updateStatus(JobDTO dto, Integer percentComplete, JobStatusType status)
            throws EntityRetrievalException;

    void addJobMessage(JobDTO job, String message);

    JobDTO update(JobDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<JobDTO> findAll();

    List<JobDTO> findAllRunning();

    List<JobDTO> findAllRunningAndCompletedBetweenDates(Date startDate, Date endDate, Long userId);

    List<JobTypeDTO> findAllTypes();

    JobDTO getById(Long id);

    List<JobDTO> getByUser(Long contactId);
}
