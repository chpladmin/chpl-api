package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestFunctionalityDTO;

public interface TestFunctionalityDAO {
    public List<TestFunctionalityDTO> findAll();

    public TestFunctionalityDTO getById(Long id) throws EntityRetrievalException;

    public TestFunctionalityDTO getByNumberAndEdition(String number, Long editionId);
}
