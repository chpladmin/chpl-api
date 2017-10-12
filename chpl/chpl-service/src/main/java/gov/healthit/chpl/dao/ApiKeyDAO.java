package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyDTO;

public interface ApiKeyDAO {

    ApiKeyDTO create(ApiKeyDTO apiKey) throws EntityCreationException;

    ApiKeyDTO update(ApiKeyDTO apiKey) throws EntityRetrievalException;

    void delete(Long id);

    List<ApiKeyDTO> findAll();

    ApiKeyDTO getById(Long id) throws EntityRetrievalException;

    ApiKeyDTO getByKey(String apiKey) throws EntityRetrievalException;

    List<ApiKeyDTO> findAllRevoked();

    ApiKeyDTO getRevokedKeyById(Long id) throws EntityRetrievalException;

    ApiKeyDTO getRevokedKeyByKey(String apiKey);

}
