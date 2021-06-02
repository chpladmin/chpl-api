package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantAgeStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ParticipantAgeStatisticsDAO {
    /**
     * Retrieves all of the participant_age_statisticst records as a list ParticipantsAgeStatisticsDTO objects.
     * @return List<ParticipantAgeStatisticsDTO>
     */
    List<ParticipantAgeStatisticsDTO> findAll();

    /**
     * Marks the participant_age_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ParticipantAgeStatisticsDTO into the participant_count_statistics table.
     * @param dto ParticipantAgeStatisticsDTO object populated with the data to be inserted.
     * @return ParticipantAgeStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ParticipantAgeStatisticsEntity create(ParticipantAgeStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;

}
