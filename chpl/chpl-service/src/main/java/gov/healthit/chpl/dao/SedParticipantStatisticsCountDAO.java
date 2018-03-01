package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.entity.SedParticipantStatisticsCountEntity;

public interface SedParticipantStatisticsCountDAO {
	List<SedParticipantStatisticsCountDTO> findAll();
	
	void delete(Long id) throws EntityRetrievalException;
	
	SedParticipantStatisticsCountEntity create(SedParticipantStatisticsCountDTO dto) throws EntityCreationException, EntityRetrievalException;
}
