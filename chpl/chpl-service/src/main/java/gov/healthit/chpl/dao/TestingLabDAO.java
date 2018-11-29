package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestingLabDAO {

    public TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException;

    public TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException;

    public List<TestingLabDTO> findAll();
    public List<TestingLabDTO> findAllActive();

    public TestingLabDTO getById(Long id) throws EntityRetrievalException;

    public TestingLabDTO getByName(String name);

    public String getMaxCode();
}
