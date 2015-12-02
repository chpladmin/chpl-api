package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyActivityDTO;

public interface ApiKeyActivityDAO {

	public ApiKeyActivityDTO create(ApiKeyActivityDTO apiKeyActivityDto) throws EntityCreationException;
	public ApiKeyActivityDTO update(ApiKeyActivityDTO apiKeyActivityDto) throws EntityRetrievalException;
	public void delete(Long id);
	public List<ApiKeyActivityDTO> findAll();
	public List<ApiKeyActivityDTO> findAll(Integer pageNumber, Integer pageSize);
	public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId);
	public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId, Integer pageNumber, Integer pageSize);
	public ApiKeyActivityDTO getById(Long id) throws EntityRetrievalException;
	
	
}
