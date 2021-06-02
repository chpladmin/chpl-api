package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ParticipantExperienceStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;


/**
 * Data access object for the participant_experience_statistics table.
 * @author TYoung
 *
 */
public interface ParticipantExperienceStatisticsDAO {
    /**
     * Retrieves all of the participant_experience_statistics records as a list ParticipantsExperienceStatisticsDTO
     * objects.
     * @param experienceTypeID 1 - Professional Experience, 2 - Product Experience, 3 - Computer Experience.
     *          These values have constants defined in ExperienceTypes.
     * @return List<ParticipantProfExperienceStatisticsDTO>
     */
    List<ParticipantExperienceStatisticsDTO> findAll(Long experienceTypeID);

    /**
     * Marks the participant_experience_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ParticipantExperienceStatisticsDTO into the participant_experience_statistics table.
     * @param dto ParticipantExperienceStatisticsDTO object populated with the data to be inserted.
     * @return ParticipantExperienceStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ParticipantExperienceStatisticsEntity create(ParticipantExperienceStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;

}
