package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface SummaryStatisticsDAO {
    SummaryStatisticsEntity create(SummaryStatisticsEntity summaryStatistics)
            throws EntityCreationException, EntityRetrievalException;

    SummaryStatisticsEntity getCurrentSummaryStatistics() throws EntityRetrievalException;

    void deleteAll();
}
