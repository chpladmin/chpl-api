package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.entity.statistics.SedParticipantStatisticsCountEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the sed_participant_statistics_count table.
 * @author TYoung
 *
 */
public interface SedParticipantStatisticsCountDAO {
    /**
     * Retrieves all of the sed_participant_statistics_count records as a list SedParticipantStatisticsCountDTO objects.
     * @return List<SedParticipantStatisticsCountDTO>
     */
    List<SedParticipantStatisticsCountDTO> findAll();

    /**
     * Marks the sed_participant_statistics_count record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the SedParticipantStatisticsCountDTO into the sed_participant_statistics_count table.
     * @param dto SedParticipantStatisticsCountDTO object populated with the data to be inserted.
     * @return SedParticipantStatisticsCountEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    SedParticipantStatisticsCountEntity create(SedParticipantStatisticsCountDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
