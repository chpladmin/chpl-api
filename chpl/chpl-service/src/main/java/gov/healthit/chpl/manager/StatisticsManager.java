package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;


public interface StatisticsManager {
	List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts();
}
