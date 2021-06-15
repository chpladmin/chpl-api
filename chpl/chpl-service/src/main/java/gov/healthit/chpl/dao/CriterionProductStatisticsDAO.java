package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.entity.statistics.CriterionProductStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;


/**
 * Data access object for the criterion_product_statistics table.
 * @author alarned
 *
 */
public interface CriterionProductStatisticsDAO {
    /**
     * Retrieves all of the criterion_product_statistics records as a list ParticipantsEducationStatisticsDTO
     * objects.
     * @return List<CriterionProductStatisticsDTO>
     */
    List<CriterionProductStatisticsDTO> findAll();

    /**
     * Marks the criterion_product_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the CriterionProductStatisticsDTO into the criterion_product_statistics table.
     * @param dto CriterionProductStatisticsDTO object populated with the data to be inserted.
     * @return CriterionProductStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    CriterionProductStatisticsEntity create(CriterionProductStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
