package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestToolDAO {

    TestToolDTO create(TestToolDTO dto) throws EntityCreationException, EntityRetrievalException;

    TestToolDTO update(TestToolDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<TestToolDTO> findAll();

    TestToolDTO getById(Long id);

    TestToolDTO getByName(String name);
}
