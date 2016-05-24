package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestingLabDTO;

public interface TestingLabDAO {
	
	public TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestingLabDTO> findAll(boolean showDeleted);
	
	public TestingLabDTO getById(Long id) throws EntityRetrievalException;
	public TestingLabDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;
	public TestingLabDTO getByName(String name);
	public String getMaxCode();
}
