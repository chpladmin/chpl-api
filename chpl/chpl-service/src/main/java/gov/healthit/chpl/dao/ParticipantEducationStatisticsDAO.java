package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantEducationStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;


/**
 * Data access object for the participant_education_statistics table.
 * @author TYoung
 *
 */
public interface ParticipantEducationStatisticsDAO {
    /**
     * Retrieves all of the participant_education_statistics records as a list ParticipantsEducationStatisticsDTO
     * objects.
     * @return List<ParticipantEducationStatisticsDTO>
     */
    List<ParticipantEducationStatisticsDTO> findAll();

    /**
     * Marks the participant_education_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ParticipantEducationStatisticsDTO into the participant_education_statistics table.
     * @param dto ParticipantEducationStatisticsDTO object populated with the data to be inserted.
     * @return ParticipantEducationStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ParticipantEducationStatisticsEntity create(ParticipantEducationStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
