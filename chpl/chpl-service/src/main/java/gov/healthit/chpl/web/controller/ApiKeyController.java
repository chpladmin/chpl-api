package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKey;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.domain.ApiKeyRegistration;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.manager.ApiKeyManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/key")
public class ApiKeyController {
	
	@Autowired
	private ApiKeyManager apiKeyManager;

	
	@RequestMapping(value="/register", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String register(@RequestBody ApiKeyRegistration registration) throws EntityCreationException {
		
		Date now = new Date();
		
		String apiKey = Util.md5(registration.getName() + registration.getEmail() + now.getTime() );
		ApiKeyDTO toCreate = new ApiKeyDTO();
		
		toCreate.setApiKey(apiKey);
		toCreate.setEmail(registration.getEmail());
		toCreate.setNameOrganization(registration.getName());
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		
		apiKeyManager.createKey(toCreate);
		
		return "{\"keyRegistered\" : \""+apiKey+"\" }";
	}
	
	@RequestMapping(value="/revoke", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String revoke(@RequestBody ApiKey key) throws EntityCreationException {
		
		String keyString = key.getKey();
		apiKeyManager.findKey(keyString);
		return "{\"keyRevoked\" : \""+keyString+"\" }";
		
	}
	
	
	@RequestMapping(value="/list", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public List<ApiKey> listKeys() throws EntityCreationException {
		
		List<ApiKey> keys = new ArrayList<ApiKey>();
		List<ApiKeyDTO> dtos = apiKeyManager.findAll();
		
		for (ApiKeyDTO dto : dtos){
			ApiKey apiKey = new ApiKey();
			apiKey.setName(dto.getNameOrganization());
			apiKey.setEmail(dto.getEmail());
			apiKey.setKey(dto.getApiKey());
			keys.add(apiKey);
		}
		
		return keys;
	}
	
	@RequestMapping(value="/activity", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public List<ApiKeyActivity> listActivity(
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws EntityRetrievalException
	{
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		if (pageSize == null){
			pageSize = 100;
		}
		
		List<ApiKeyActivity> activity = apiKeyManager.getApiKeyActivity(pageNumber, pageSize);
		
		return activity;
		
	}
	
	@RequestMapping(value="/activity/{apiKey}", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public List<ApiKeyActivity> listActivityByKey(
			@PathVariable("apiKey") String apiKey,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws EntityRetrievalException
	{
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		if (pageSize == null){
			pageSize = 100;
		}
		
		List<ApiKeyActivity> activity = apiKeyManager.getApiKeyActivity(apiKey, pageNumber, pageSize);
		
		return activity;
		
	}
	
	
}
