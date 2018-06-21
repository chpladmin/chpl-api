package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestingLabDAO {

    TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException;

    TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<TestingLabDTO> findAll(boolean showDeleted);

    TestingLabDTO getById(Long id) throws EntityRetrievalException;

    TestingLabDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;

    TestingLabDTO getByName(String name);

    String getMaxCode();
}
