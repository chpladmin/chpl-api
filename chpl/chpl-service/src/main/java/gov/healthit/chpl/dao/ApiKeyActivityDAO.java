package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ApiKeyActivityDAO {

    ApiKeyActivityDTO create(ApiKeyActivityDTO apiKeyActivityDto) throws EntityCreationException;

    ApiKeyActivityDTO update(ApiKeyActivityDTO apiKeyActivityDto) throws EntityRetrievalException;

    void delete(Long id);
}
