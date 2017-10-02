package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.EducationTypeDTO;

public interface EducationTypeDAO {
    EducationTypeDTO getById(Long id) throws EntityRetrievalException;

    List<EducationTypeDTO> getAll();

    EducationTypeDTO getByName(String typeName);
}
