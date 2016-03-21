package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestTaskDTO;

public interface TestTaskDAO {
	
	public TestTaskDTO create(TestTaskDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestTaskDTO update(TestTaskDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestTaskDTO> findAll();
	public TestTaskDTO getById(Long id) throws EntityRetrievalException;
}
