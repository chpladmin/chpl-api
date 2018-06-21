package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface PracticeTypeDAO {

    void create(PracticeTypeDTO dto) throws EntityCreationException, EntityRetrievalException;

    void update(PracticeTypeDTO dto) throws EntityRetrievalException;

    void delete(Long id);

    List<PracticeTypeDTO> findAll();

    PracticeTypeDTO getById(Long id) throws EntityRetrievalException;

    PracticeTypeDTO getByName(String name);

}
