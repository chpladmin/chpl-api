package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AgeRangeDTO;

public interface AgeRangeDAO {
    AgeRangeDTO getById(Long id) throws EntityRetrievalException;

    List<AgeRangeDTO> getAll();

    AgeRangeDTO getByName(String typeName);
}
