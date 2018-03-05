package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;

/**
 * The StatisticsManager class is used to retrieve data that will be used for charting.
 * @author TYoung
 *
 */
public interface StatisticsManager {
    /**
     * Retrieves that data that will be used for the SED and participant counts chart.
     * @return List of SedParticipantStatisticsCountDTO objects
     */
    List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts();
}
