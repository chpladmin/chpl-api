package gov.healthit.chpl.dao.scheduler;

import java.util.List;

import gov.healthit.chpl.dto.scheduler.BrokenSurveillanceRulesDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data access object for the broken_surveillance_rules table.
 * @author alarned
 *
 */
public interface BrokenSurveillanceRulesDAO {
    /**
     * Retrieves all of the broken_surveillance_rules records as a list BrokenSurveillanceRulesDTO objects.
     * @return List<BrokenSurveillanceRulesDTO> all records
     */
    List<BrokenSurveillanceRulesDTO> findAll();

    /**
     * Marks all broken_surveillance_rules records as deleted.
     */
    void deleteAll();

    /**
     * Inserts the data in the BrokenSurveillanceRulesDTO into the broken_surveillance_rules table.
     * @param dto BrokenSurveillanceRulesDTO object populated with the data to be inserted.
     * @throws EntityCreationException when the data could not be inserted
     * @throws EntityRetrievalException when the newly created record cannot be retrieved
     */
    void create(List<BrokenSurveillanceRulesDTO> dto)
            throws EntityCreationException, EntityRetrievalException;
}
