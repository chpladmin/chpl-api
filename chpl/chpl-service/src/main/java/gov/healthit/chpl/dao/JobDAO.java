package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.dto.JobTypeDTO;

public interface JobDAO {
	
	public JobDTO create(JobDTO dto) throws EntityCreationException;	
	public JobDTO update(JobDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<JobDTO> findAll();
	public List<JobDTO> findAllRunning();
	public List<JobDTO> findAllRunningAndCompletedBetweenDates(Date startDate, Date endDate);
	public List<JobTypeDTO> findAllTypes();
	public JobDTO getById(Long id);
	public List<JobDTO> getByUser(Long contactId);
}
