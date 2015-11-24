package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyActivityDTO;

public interface ApiKeyActivityDAO {

	public ApiKeyActivityDTO create(ApiKeyActivityDTO apiKey) throws EntityCreationException;
	public ApiKeyActivityDTO update(ApiKeyActivityDTO apiKey) throws EntityRetrievalException;
	public void delete(Long id);
	public List<ApiKeyActivityDTO> findAll();
	public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId);
	public ApiKeyActivityDTO getById(Long id) throws EntityRetrievalException;
	
	
}
