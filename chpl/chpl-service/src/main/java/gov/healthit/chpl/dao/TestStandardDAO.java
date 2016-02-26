package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TestStandardDTO;

public interface TestStandardDAO {
	
	public TestStandardDTO create(TestStandardDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestStandardDTO update(TestStandardDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestStandardDTO> findAll();
	public TestStandardDTO getById(Long id) throws EntityRetrievalException;
	public TestStandardDTO getByNumber(String name);
}
