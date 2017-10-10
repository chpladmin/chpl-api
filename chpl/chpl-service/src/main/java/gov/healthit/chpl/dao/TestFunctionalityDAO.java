package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestFunctionalityDTO;

public interface TestFunctionalityDAO {
    List<TestFunctionalityDTO> findAll();

    TestFunctionalityDTO getById(Long id) throws EntityRetrievalException;

    TestFunctionalityDTO getByNumberAndEdition(String number, Long editionId);
}
