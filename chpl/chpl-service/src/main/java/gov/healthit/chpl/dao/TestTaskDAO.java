package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestTaskDTO;

public interface TestTaskDAO {

    TestTaskDTO create(TestTaskDTO dto) throws EntityCreationException;

    TestTaskDTO update(TestTaskDTO dto) throws EntityRetrievalException;

    void delete(Long id);

    List<TestTaskDTO> findAll();

    TestTaskDTO getById(Long id);
}
