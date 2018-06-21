package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface AgeRangeDAO {
    AgeRangeDTO getById(Long id) throws EntityRetrievalException;

    List<AgeRangeDTO> getAll();

    AgeRangeDTO getByName(String typeName);
}
