package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.web.controller.results.DeveloperResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(value = "developers")
@RestController
@RequestMapping("/developers")
public class DeveloperController {
	
	@Autowired DeveloperManager developerManager;
	@Autowired ProductManager productManager;
	@Autowired CertifiedProductManager cpManager;
	
	@ApiOperation(value="List all developers in the system.", 
			notes="")
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody DeveloperResults getDevelopers(){
		List<DeveloperDTO> developerList = developerManager.getAll();		
		
		List<Developer> developers = new ArrayList<Developer>();
		if(developerList != null && developerList.size() > 0) {
			for(DeveloperDTO dto : developerList) {
				Developer result = new Developer(dto);
				developers.add(result);
			}
		}
		
		DeveloperResults results = new DeveloperResults();
		results.setDevelopers(developers);
		return results;
	}
	
	@ApiOperation(value="Get information about a specific developer.", 
			notes="")
	@RequestMapping(value="/{developerId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Developer getDeveloperById(@PathVariable("developerId") Long developerId) throws EntityRetrievalException {
		DeveloperDTO developer = developerManager.getById(developerId);
		
		Developer result = null;
		if(developer != null) {
			result = new Developer(developer);
		}
		return result;
	}
	
	@ApiOperation(value="Update a developer or merge developers.", 
			notes="This method serves two purposes: to update a single developer's information and to merge two developers into one. "
					+ " A user of this service should pass in a single developerId to update just that developer. "
					+ " If multiple developer IDs are passed in, the service performs a merge meaning that a new developer "
					+ " is created with all of the information provided (name, address, etc.) and all of the prodcuts "
					+ " previously assigned to the developerId's specified are reassigned to the newly created developer. The "
					+ " old developers are then deleted. "
					+ " The logged in user must have ROLE_ADMIN, ROLE_ACB_ADMIN, or ROLE_ACB_STAFF. ")
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Developer updateDeveloper(@RequestBody(required=true) UpdateDevelopersRequest developerInfo) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		DeveloperDTO result = null;
		
		if(developerInfo.getDeveloperIds().size() > 1) {
			//merge these developers into one 
			// - create a new developer with the rest of the passed in information
			DeveloperDTO toCreate = new DeveloperDTO();
			toCreate.setDeveloperCode(developerInfo.getDeveloper().getDeveloperCode());
			toCreate.setName(developerInfo.getDeveloper().getName());
			toCreate.setWebsite(developerInfo.getDeveloper().getWebsite());
			toCreate.setTransparencyAttestation(developerInfo.getDeveloper().getTransparencyAttestation());
			
			Address developerAddress = developerInfo.getDeveloper().getAddress();
			if(developerAddress != null) {
				AddressDTO toCreateAddress = new AddressDTO();
				toCreateAddress.setStreetLineOne(developerAddress.getLine1());
				toCreateAddress.setStreetLineTwo(developerAddress.getLine2());
				toCreateAddress.setCity(developerAddress.getCity());
				toCreateAddress.setState(developerAddress.getState());
				toCreateAddress.setZipcode(developerAddress.getZipcode());
				toCreateAddress.setCountry(developerAddress.getCountry());
				toCreate.setAddress(toCreateAddress);
			}
			Contact developerContact = developerInfo.getDeveloper().getContact();
			ContactDTO toCreateContact = new ContactDTO();
			toCreateContact.setEmail(developerContact.getEmail());
			toCreateContact.setFirstName(developerContact.getFirstName());
			toCreateContact.setLastName(developerContact.getLastName());
			toCreateContact.setPhoneNumber(developerContact.getPhoneNumber());
			toCreateContact.setTitle(developerContact.getTitle());
			toCreate.setContact(toCreateContact);
			result = developerManager.merge(developerInfo.getDeveloperIds(), toCreate);
			//re-query because the developer code isn't filled in otherwise
			result = developerManager.getById(result.getId());
		} else if(developerInfo.getDeveloperIds().size() == 1) {
			//update the information for the developer id supplied in the database
			DeveloperDTO toUpdate = new DeveloperDTO();
			toUpdate.setDeveloperCode(developerInfo.getDeveloper().getDeveloperCode());
			toUpdate.setId(developerInfo.getDeveloperIds().get(0));
			toUpdate.setName(developerInfo.getDeveloper().getName());
			toUpdate.setWebsite(developerInfo.getDeveloper().getWebsite());
			toUpdate.setTransparencyAttestation(developerInfo.getDeveloper().getTransparencyAttestation());
			
			if(developerInfo.getDeveloper().getAddress() != null) {
				AddressDTO address = new AddressDTO();
				address.setId(developerInfo.getDeveloper().getAddress().getAddressId());
				address.setStreetLineOne(developerInfo.getDeveloper().getAddress().getLine1());
				address.setStreetLineTwo(developerInfo.getDeveloper().getAddress().getLine2());
				address.setCity(developerInfo.getDeveloper().getAddress().getCity());
				address.setState(developerInfo.getDeveloper().getAddress().getState());
				address.setZipcode(developerInfo.getDeveloper().getAddress().getZipcode());
				address.setCountry(developerInfo.getDeveloper().getAddress().getCountry());
				toUpdate.setAddress(address);
			}
			if(developerInfo.getDeveloper().getContact() != null) {
				Contact developerContact = developerInfo.getDeveloper().getContact();
				ContactDTO toUpdateContact = new ContactDTO();
				toUpdateContact.setEmail(developerContact.getEmail());
				toUpdateContact.setFirstName(developerContact.getFirstName());
				toUpdateContact.setLastName(developerContact.getLastName());
				toUpdateContact.setPhoneNumber(developerContact.getPhoneNumber());
				toUpdateContact.setTitle(developerContact.getTitle());
				toUpdate.setContact(toUpdateContact);
			}
			result = developerManager.update(toUpdate);
		}
		
		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the developer information.");
		}
		Developer restResult = new Developer(result);
		return restResult;
	}
}
