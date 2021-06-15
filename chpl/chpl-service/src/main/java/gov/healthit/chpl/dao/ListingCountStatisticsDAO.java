package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ListingCountStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the listing_count_statistics table.
 * @author alarned
 *
 */
public interface ListingCountStatisticsDAO {
    /**
     * Retrieves all of the active_listings_statistics records as a list ListingCountStatisticsDTO objects.
     * @return List<ListingCountStatisticsDTO> all records
     */
    List<ListingCountStatisticsDTO> findAll();

    /**
     * Marks the listing_count_statistics record that is associated with the id as deleted.
     * @param id Id of the record to be marked for deletion
     * @throws EntityRetrievalException When the record could not be found.
     */
    void delete(Long id) throws EntityRetrievalException;

    /**
     * Inserts the data in the ListingCountStatisticsDTO into the listing_count_statistics table.
     * @param dto ListingCountStatisticsDTO object populated with the data to be inserted.
     * @return ListingCountStatisticsEntity representing the data that was inserted
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    ListingCountStatisticsEntity create(ListingCountStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException;
}
