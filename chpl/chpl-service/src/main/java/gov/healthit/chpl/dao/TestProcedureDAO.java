package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestProcedureDTO;

public interface TestProcedureDAO {
	
	public TestProcedureDTO create(TestProcedureDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TestProcedureDTO update(TestProcedureDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestProcedureDTO> findAll();
	public TestProcedureDTO getById(Long id) throws EntityRetrievalException;
	public TestProcedureDTO getByName(String name);
}
