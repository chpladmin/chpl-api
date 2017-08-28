package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.JobDTO;

public interface JobDAO {
	
	public JobDTO create(JobDTO dto) throws EntityCreationException;	
	public JobDTO update(JobDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<JobDTO> findAll();
	public JobDTO getById(Long id);
	public JobDTO getByUser(Long contactId);
}
