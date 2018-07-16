package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface TestParticipantDAO {

    TestParticipantDTO create(TestParticipantDTO dto) throws EntityCreationException;

    TestParticipantDTO update(TestParticipantDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<TestParticipantDTO> findAll();

    TestParticipantDTO getById(Long id);
}
