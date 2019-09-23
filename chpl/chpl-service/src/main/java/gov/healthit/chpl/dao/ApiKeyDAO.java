package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * @author TYoung
 *
 */
public interface ApiKeyDAO {

    ApiKeyDTO create(ApiKeyDTO apiKey) throws EntityCreationException;

    ApiKeyDTO update(ApiKeyDTO apiKey) throws EntityRetrievalException;

    void delete(Long id);

    List<ApiKeyDTO> findAll(Boolean includeDeleted);

    ApiKeyDTO getById(Long id) throws EntityRetrievalException;

    ApiKeyDTO getByKey(String apiKey) throws EntityRetrievalException;

    List<ApiKeyDTO> findAllRevoked();

    ApiKeyDTO getRevokedKeyById(Long id) throws EntityRetrievalException;

    ApiKeyDTO getRevokedKeyByKey(String apiKey);

    List<ApiKeyDTO> findAllWhitelisted();

    /**
     * Returns list of ApiKeyDTO objects. The list is based on objects where:
     * deleted = false lastUsedDate < current date + days
     * 
     * @param days
     *            - days since the api key was last used
     * @return List of ApiKeyDTO objects meeting criteria
     */
    List<ApiKeyDTO> findAllNotUsedInXDays(Integer days);

    /**
     * Returns list of ApiKeyDTO objects. The list is based on objects where:
     * deleted = false deleteWarningSentDate < current date +
     * daysSinceWarningSent
     * 
     * @param daysSinceWarningSent
     *            - integer
     * @return List of ApiKeyDTO objects meeting criteria
     */
    List<ApiKeyDTO> findAllToBeRevoked(Integer daysSinceWarningSent);
}
