package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.manager.ApiKeyManager;

@Service
public class ApiKeyManagerImpl implements ApiKeyManager {
	
	@Autowired
	private ApiKeyDAO apiKeyDAO;
	
	@Autowired
	private ApiKeyActivityDAO apiKeyActivityDAO;
	
	@Override
	@Transactional
	public ApiKeyDTO createKey(ApiKeyDTO toCreate) throws EntityCreationException {
		return apiKeyDAO.create(toCreate);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteKey(Long keyId) {
		apiKeyDAO.delete(keyId);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteKey(String keyString) {
		Long keyId = this.findKey(keyString).getId();
		apiKeyDAO.delete(keyId);
	}

	@Override
	@Transactional
	public ApiKeyDTO findKey(Long keyId) throws EntityRetrievalException {
		return apiKeyDAO.getById(keyId);
	}

	@Override
	@Transactional
	public ApiKeyDTO findKey(String keyString) {
		return apiKeyDAO.getByKey(keyString);
	}

	@Override
	@Transactional
	public void logApiKeyActivity(String keyString, String apiCallPath) throws EntityCreationException {
		
		ApiKeyDTO apiKey = findKey(keyString);
		ApiKeyActivityDTO apiKeyActivityDto = new ApiKeyActivityDTO();
		
		apiKeyActivityDto.setApiCallPath(apiCallPath);
		apiKeyActivityDto.setApiKeyId(apiKey.getId());
		apiKeyActivityDto.setDeleted(false);
		
		apiKeyActivityDAO.create(apiKeyActivityDto);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ApiKeyActivity> getApiKeyActivity(String keyString) {
		
		ApiKeyDTO apiKey = findKey(keyString);
		List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findByKeyId(apiKey.getId());
		List<ApiKeyActivity> activity = new ArrayList<ApiKeyActivity>();
		
		for (ApiKeyActivityDTO dto : activityDTOs){
			
			ApiKeyActivity apiKeyActivity = new ApiKeyActivity();
			
			apiKeyActivity.setApiKey(apiKey.getApiKey());
			apiKeyActivity.setApiKeyId(apiKey.getId());
			apiKeyActivity.setEmail(apiKey.getEmail());
			apiKeyActivity.setName(apiKey.getNameOrganization());
			apiKeyActivity.setId(dto.getId());
			apiKeyActivity.setCreationDate(dto.getCreationDate());
			apiKeyActivity.setApiCallPath(dto.getApiCallPath());
			
			activity.add(apiKeyActivity);
			
		}
		return activity;
	}
	
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ApiKeyActivity> getApiKeyActivity(String keyString, Integer pageNumber, Integer pageSize) {
		
		ApiKeyDTO apiKey = findKey(keyString);
		List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findByKeyId(apiKey.getId(), pageNumber, pageSize);
		List<ApiKeyActivity> activity = new ArrayList<ApiKeyActivity>();
		
		for (ApiKeyActivityDTO dto : activityDTOs){
			
			ApiKeyActivity apiKeyActivity = new ApiKeyActivity();
			
			apiKeyActivity.setApiKey(apiKey.getApiKey());
			apiKeyActivity.setApiKeyId(apiKey.getId());
			apiKeyActivity.setEmail(apiKey.getEmail());
			apiKeyActivity.setName(apiKey.getNameOrganization());
			apiKeyActivity.setId(dto.getId());
			apiKeyActivity.setCreationDate(dto.getCreationDate());
			apiKeyActivity.setApiCallPath(dto.getApiCallPath());
			
			activity.add(apiKeyActivity);
			
		}
		return activity;
	}
	
	

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ApiKeyDTO> findAll() {
		return apiKeyDAO.findAll();
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ApiKeyActivity> getApiKeyActivity() throws EntityRetrievalException {
		
		List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findAll();
		List<ApiKeyActivity> activity = new ArrayList<ApiKeyActivity>();
		
		for (ApiKeyActivityDTO dto : activityDTOs){
			
			ApiKeyDTO apiKey = findKey(dto.getApiKeyId());
			
			ApiKeyActivity apiKeyActivity = new ApiKeyActivity();
			
			apiKeyActivity.setApiKey(apiKey.getApiKey());
			apiKeyActivity.setApiKeyId(apiKey.getId());
			apiKeyActivity.setEmail(apiKey.getEmail());
			apiKeyActivity.setName(apiKey.getNameOrganization());
			apiKeyActivity.setId(dto.getId());
			apiKeyActivity.setCreationDate(dto.getCreationDate());
			apiKeyActivity.setApiCallPath(dto.getApiCallPath());
			
			activity.add(apiKeyActivity);
			
		}
		return activity;
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ApiKeyActivity> getApiKeyActivity(Integer pageNumber, Integer pageSize) throws EntityRetrievalException {
		
		List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findAll(pageNumber, pageSize) ;
		List<ApiKeyActivity> activity = new ArrayList<ApiKeyActivity>();
		
		for (ApiKeyActivityDTO dto : activityDTOs){
			
			ApiKeyDTO apiKey = findKey(dto.getApiKeyId());
			
			ApiKeyActivity apiKeyActivity = new ApiKeyActivity();
			
			apiKeyActivity.setApiKey(apiKey.getApiKey());
			apiKeyActivity.setApiKeyId(apiKey.getId());
			apiKeyActivity.setEmail(apiKey.getEmail());
			apiKeyActivity.setName(apiKey.getNameOrganization());
			apiKeyActivity.setId(dto.getId());
			apiKeyActivity.setCreationDate(dto.getCreationDate());
			apiKeyActivity.setApiCallPath(dto.getApiCallPath());
			
			activity.add(apiKeyActivity);
			
		}
		return activity;
	}
	
}
