package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobStatusDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.job.JobStatusType;

public interface JobDAO {

	public JobDTO create(JobDTO dto) throws EntityCreationException;
	public void markStarted(JobDTO dto) throws EntityRetrievalException;
	public JobStatusDTO updateStatus(JobDTO dto, Integer percentComplete, JobStatusType status) throws EntityRetrievalException;
	public void addJobMessage(JobDTO job, String message);
	public JobDTO update(JobDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;

	public List<JobDTO> findAll();
	public List<JobDTO> findAllRunning();
	public List<JobDTO> findAllRunningAndCompletedBetweenDates(Date startDate, Date endDate, Long userId);
	public List<JobTypeDTO> findAllTypes();
	public JobDTO getById(Long id);
	public List<JobDTO> getByUser(Long contactId);
}
