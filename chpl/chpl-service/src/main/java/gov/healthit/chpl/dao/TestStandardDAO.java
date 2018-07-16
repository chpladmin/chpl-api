package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.exception.EntityCreationException;

public interface TestStandardDAO {

    TestStandardDTO create(TestStandardDTO dto) throws EntityCreationException;

    List<TestStandardDTO> findAll();

    TestStandardDTO getByNumberAndEdition(String number, Long editionId);

    TestStandardDTO getById(Long id);
}
