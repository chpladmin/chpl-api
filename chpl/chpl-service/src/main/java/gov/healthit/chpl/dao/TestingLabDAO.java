package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestingLabDAO {

    TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException;

    TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException;

    List<TestingLabDTO> findAll();

    TestingLabDTO getById(Long id) throws EntityRetrievalException;

    TestingLabDTO getByName(String name);

    String getMaxCode();
}
