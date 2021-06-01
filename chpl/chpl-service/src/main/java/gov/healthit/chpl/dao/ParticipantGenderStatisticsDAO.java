package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantGenderStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the participant_gender_statistics table.
 * @author TYoung
 *
 */
public interface ParticipantGenderStatisticsDAO {
    /**
     * Retrieves all of the participant_gender_statistics records as a list ParticipantGenderStatisticsDTO objects.
     * @return List<ParticipantGenderStatisticsDTO>
     */
    List<ParticipantGenderStatisticsDTO> findAll();

    /**
     * Marks the participant_gender_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ParticipantGenderStatisticsDTO into the participant_gender_statistics table.
     * @param dto ParticipantGenderStatisticsDTO object populated with the data to be inserted.
     * @return ParticipantGenderStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ParticipantGenderStatisticsEntity create(ParticipantGenderStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
