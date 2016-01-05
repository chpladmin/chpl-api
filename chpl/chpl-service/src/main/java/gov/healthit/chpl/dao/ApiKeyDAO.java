package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyDTO;

public interface ApiKeyDAO {

	public ApiKeyDTO create(ApiKeyDTO apiKey) throws EntityCreationException;
	public ApiKeyDTO update(ApiKeyDTO apiKey) throws EntityRetrievalException;
	public void delete(Long id);
	public List<ApiKeyDTO> findAll();
	public ApiKeyDTO getById(Long id) throws EntityRetrievalException;
	public ApiKeyDTO getByKey(String apiKey);
	public List<ApiKeyDTO> findAllRevoked();
	public ApiKeyDTO getRevokedKeyById(Long id) throws EntityRetrievalException;
	public ApiKeyDTO getRevokedKeyByKey(String apiKey);
	
}
