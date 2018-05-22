package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ActiveListingsStatisticsDTO;
import gov.healthit.chpl.entity.ActiveListingsStatisticsEntity;

/**
 * Data access object for the active_listings_statistics table.
 * @author alarned
 *
 */
public interface ActiveListingsStatisticsDAO {
    /**
     * Retrieves all of the active_listings_statistics records as a list ActiveListingsStatisticsDTO objects.
     * @return List<ActiveListingsStatisticsDTO>
     */
    List<ActiveListingsStatisticsDTO> findAll();

    /**
     * Marks the active_listings_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ActiveListingsStatisticsDTO into the active_listings_statistics table.
     * @param dto ActiveListingsStatisticsDTO object populated with the data to be inserted.
     * @return ActiveListingsStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ActiveListingsStatisticsEntity create(ActiveListingsStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
