package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestParticipantDTO;

public interface TestParticipantDAO {
	
	public TestParticipantDTO create(TestParticipantDTO dto) throws EntityCreationException;	
	public TestParticipantDTO update(TestParticipantDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TestParticipantDTO> findAll();
	public TestParticipantDTO getById(Long id);
}
