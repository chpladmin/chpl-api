package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterDAO {
    public List<QuarterDTO> getAll();
    public QuarterDTO getById(Long id) throws EntityRetrievalException;
}
