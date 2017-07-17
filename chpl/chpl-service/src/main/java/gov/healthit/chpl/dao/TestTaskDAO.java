package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestTaskDTO;

public interface TestTaskDAO {
	
	public TestTaskDTO create(TestTaskDTO dto) throws EntityCreationException;	
	public TestTaskDTO update(TestTaskDTO dto) throws EntityRetrievalException;
	public void delete(Long id);
	
	public List<TestTaskDTO> findAll();
	public TestTaskDTO getById(Long id);
}
