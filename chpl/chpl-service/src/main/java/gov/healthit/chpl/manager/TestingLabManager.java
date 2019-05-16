package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;

public interface TestingLabManager {

    TestingLabDTO create(TestingLabDTO atl)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    TestingLabDTO update(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException;

    TestingLabDTO retire(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException;

    TestingLabDTO unretire(Long atlId) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException;

    List<TestingLabDTO> getAllForUser();

    List<TestingLabDTO> getAll();

    TestingLabDTO getById(Long id) throws EntityRetrievalException;

}
