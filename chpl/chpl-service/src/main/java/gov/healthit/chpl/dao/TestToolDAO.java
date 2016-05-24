package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestToolDTO;

public interface TestToolDAO {
	
	public TestToolDTO create(TestToolDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestToolDTO update(TestToolDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestToolDTO> findAll();
	public TestToolDTO getById(Long id) throws EntityRetrievalException;
	public TestToolDTO getByName(String name );
}
