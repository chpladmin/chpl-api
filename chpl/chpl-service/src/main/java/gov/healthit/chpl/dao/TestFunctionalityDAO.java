package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestFunctionalityDTO;

public interface TestFunctionalityDAO {
	
	public TestFunctionalityDTO create(TestFunctionalityDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestFunctionalityDTO update(TestFunctionalityDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestFunctionalityDTO> findAll();
	public TestFunctionalityDTO getById(Long id) throws EntityRetrievalException;
	public TestFunctionalityDTO getByNumber(String name);
}
