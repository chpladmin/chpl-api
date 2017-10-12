package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestProcedureDTO;

public interface TestProcedureDAO {

    TestProcedureDTO create(TestProcedureDTO dto) throws EntityCreationException;

    TestProcedureDTO update(TestProcedureDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<TestProcedureDTO> findAll();

    TestProcedureDTO getById(Long id);

    TestProcedureDTO getByName(String name);
}
