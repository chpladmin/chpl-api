package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestFunctionalityDAO {
    List<TestFunctionalityDTO> findAll();

    TestFunctionalityDTO getById(Long id) throws EntityRetrievalException;

    TestFunctionalityDTO getByNumberAndEdition(String number, Long editionId);
}
