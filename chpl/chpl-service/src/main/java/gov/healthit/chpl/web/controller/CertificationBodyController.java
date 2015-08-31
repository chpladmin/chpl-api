package gov.healthit.chpl.web.controller;


import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserListJSONObject;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationBodyPermission;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.web.controller.results.CertificationBodyResults;

@RestController
@RequestMapping("/acb")
public class CertificationBodyController {
	
	@Autowired CertificationBodyManager acbManager;
	@Autowired UserManager userManager;
	
	private static final Logger logger = LogManager.getLogger(CertificationBodyController.class);
	
	@RequestMapping(value="/list_acbs", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationBodyResults getAcbs() {
		List<CertificationBodyDTO> acbs = acbManager.getAll();
		
		CertificationBodyResults results = new CertificationBodyResults();
		if(acbs != null) {
			for(CertificationBodyDTO acb : acbs) {
				results.getAcbs().add(new CertificationBody(acb));
			}
		}
		return results;
	}
	
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public CertificationBody createAcb(@RequestBody CertificationBody acbInfo) throws EntityRetrievalException, EntityCreationException {
		CertificationBodyDTO toCreate = new CertificationBodyDTO();
		toCreate.setName(acbInfo.getName());
		toCreate.setWebsite(acbInfo.getWebsite());
		AddressDTO address = new AddressDTO();
		if(acbInfo.getAddress() != null) {
			address.setId(acbInfo.getAddress().getAddressId());
			address.setStreetLineOne(acbInfo.getAddress().getLine1());
			address.setStreetLineTwo(acbInfo.getAddress().getLine2());
			address.setCity(acbInfo.getAddress().getCity());
			address.setRegion(acbInfo.getAddress().getRegion());
			address.setCountry(acbInfo.getAddress().getCountry());
			address.setDeleted(false);
			address.setLastModifiedDate(new Date());
			address.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		toCreate.setAddress(address);
		toCreate = acbManager.create(toCreate);
		return new CertificationBody(toCreate);
	}
	

	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public CertificationBody updateAcb(@RequestBody CertificationBody acbInfo) throws EntityRetrievalException {
		CertificationBodyDTO toUpdate = new CertificationBodyDTO();
		toUpdate.setName(acbInfo.getName());
		toUpdate.setWebsite(acbInfo.getWebsite());
		AddressDTO address = new AddressDTO();
		if(acbInfo.getAddress() != null) {
			address.setId(acbInfo.getAddress().getAddressId());
			address.setStreetLineOne(acbInfo.getAddress().getLine1());
			address.setStreetLineTwo(acbInfo.getAddress().getLine2());
			address.setCity(acbInfo.getAddress().getCity());
			address.setRegion(acbInfo.getAddress().getRegion());
			address.setCountry(acbInfo.getAddress().getCountry());
			address.setDeleted(false);
			address.setLastModifiedDate(new Date());
			address.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		toUpdate.setAddress(address);
		
		CertificationBodyDTO result = acbManager.update(toUpdate);
		return new CertificationBody(result);
	}
	
	
	@RequestMapping(value="/delete", method= RequestMethod.DELETE,
			produces="application/json; charset=utf-8")
	public String deleteAcb(@RequestParam("acbId") Long acbId) {
		CertificationBodyDTO toDelete = new CertificationBodyDTO();
		toDelete.setId(acbId);
		acbManager.delete(toDelete);
		return "{\"deletedAcb\" : true }";
	}
	
	@RequestMapping(value="/add_user/{acbId}", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String addUserToAcb(@PathVariable("acbId") Long acbId,
								@RequestParam("userId") Long userId,
								@RequestParam("authority") CertificationBodyPermission authority) 
									throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {
		
		UserDTO user = userManager.getById(userId);
		CertificationBodyDTO acb = acbManager.getById(acbId);
		
		if(user == null || acb == null) {
			throw new InvalidArgumentsException("Could not find either ACB or User specified");
		}

		Permission permission = null;
		if(authority == CertificationBodyPermission.STAFF) {
			permission = BasePermission.READ;
		} else if(authority == CertificationBodyPermission.ADMIN) {
			permission = BasePermission.ADMINISTRATION;
		}
		acbManager.addPermission(acb, new PrincipalSid(user.getSubjectName()), permission);
		return "{\"userAdded\" : true }";
	}
	
	@RequestMapping(value="/delete_user/{acbId}", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String deleteUserFromAcb(@PathVariable("acbId") Long acbId,
								@RequestParam("userId") Long userId,
								@RequestParam(value="authority", required=false) CertificationBodyPermission authority) 
								throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException{
		
		UserDTO user = userManager.getById(userId);
		CertificationBodyDTO acb = acbManager.getById(acbId);
		
		if(user == null || acb == null) {
			throw new InvalidArgumentsException("Could not find either ACB or User specified");
		}
		
		if(authority == null) {
			//delete all permissions on that acb
			acbManager.deleteAllPermissionsOnAcb(acb, new PrincipalSid(user.getSubjectName()));
		} else {
			Permission permission = null;
			if(authority == CertificationBodyPermission.STAFF) {
				permission = BasePermission.READ;
			} else if(authority == CertificationBodyPermission.ADMIN) {
				permission = BasePermission.ADMINISTRATION;
			}
			acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), permission);
		}
		return "{\"userDeleted\" : true }";
	}
	
	@RequestMapping(value="/list_users/{acbId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserListJSONObject getUsers(@PathVariable("acbId") Long acbId){
		CertificationBodyDTO acb = new CertificationBodyDTO();
		acb.setId(acbId);
		List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
		
		UserListJSONObject result = new UserListJSONObject();
		if(users != null) {
			for(UserDTO user : users) {
				UserInfoJSONObject userInfo = new UserInfoJSONObject(user);
				result.getUsers().add(userInfo);
			}
		}
		return result;
	}
}
