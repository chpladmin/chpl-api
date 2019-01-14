package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

import java.util.List;

/**
 * Interface for database access to nonconformity statistics.
 * @author kekey
 *
 */
public interface NonconformityTypeStatisticsDAO {
    List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics();
    void create(NonconformityTypeStatisticsDTO dto);
    void deleteAllOldNonConformityStatistics() throws EntityRetrievalException;
}
