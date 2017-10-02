package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.EducationTypeDTO;

public interface EducationTypeDAO {
    public EducationTypeDTO getById(Long id) throws EntityRetrievalException;

    public List<EducationTypeDTO> getAll();

    public EducationTypeDTO getByName(String typeName);
}
