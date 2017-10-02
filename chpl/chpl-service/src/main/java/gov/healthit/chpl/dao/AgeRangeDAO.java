package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AgeRangeDTO;

public interface AgeRangeDAO {
    public AgeRangeDTO getById(Long id) throws EntityRetrievalException;

    public List<AgeRangeDTO> getAll();

    public AgeRangeDTO getByName(String typeName);
}
