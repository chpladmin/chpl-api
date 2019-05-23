package gov.healthit.chpl.dao.surveillance.report;

import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterDAO {
    public QuarterDTO getById(Long id) throws EntityRetrievalException;
}
