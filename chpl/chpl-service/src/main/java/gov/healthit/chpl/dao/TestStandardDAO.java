package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestStandardDTO;

public interface TestStandardDAO {
	
	public TestStandardDTO create(TestStandardDTO dto) throws EntityCreationException;		
	public List<TestStandardDTO> findAll();
	public TestStandardDTO getByNumberAndEdition(String number, Long editionId);
	public TestStandardDTO getById(Long id);
}
