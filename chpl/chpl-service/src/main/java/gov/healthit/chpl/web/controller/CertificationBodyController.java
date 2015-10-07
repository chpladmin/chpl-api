package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationBodyPermission;
import gov.healthit.chpl.domain.CertificationBodyUser;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.UpdateUserAndAcbRequest;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.web.controller.results.CertificationBodyResults;
import gov.healthit.chpl.web.controller.results.CertificationBodyUserResults;

@RestController
@RequestMapping("/acbs")
public class CertificationBodyController {
	
	@Autowired CertificationBodyManager acbManager;
	@Autowired UserManager userManager;
	
	private static final Logger logger = LogManager.getLogger(CertificationBodyController.class);
	
	@RequestMapping(value="/", method=RequestMethod.GET,
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
	
	@RequestMapping(value="/{acbId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationBody getAcbById(@PathVariable("acbId") Long acbId)
		throws EntityRetrievalException {
		CertificationBodyDTO acb = acbManager.getById(acbId);
		
		return new CertificationBody(acb);
	}
	
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public CertificationBody createAcb(@RequestBody CertificationBody acbInfo) throws UserRetrievalException, EntityRetrievalException, EntityCreationException {
		CertificationBodyDTO toCreate = new CertificationBodyDTO();
		toCreate.setName(acbInfo.getName());
		toCreate.setWebsite(acbInfo.getWebsite());
		AddressDTO address = null;
		if(acbInfo.getAddress() != null) {
			address = new AddressDTO();
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
		toUpdate.setId(acbInfo.getId());
		toUpdate.setName(acbInfo.getName());
		toUpdate.setWebsite(acbInfo.getWebsite());
		AddressDTO address = null;
		if(acbInfo.getAddress() != null) {
			address = new AddressDTO();
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
	
	
	@RequestMapping(value="/{acbId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteAcb(@PathVariable("acbId") Long acbId) {
		CertificationBodyDTO toDelete = new CertificationBodyDTO();
		toDelete.setId(acbId);
		acbManager.delete(toDelete);
		return "{\"deletedAcb\" : true }";
	}
	
	@RequestMapping(value="/add_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String addUserToAcb(@RequestBody UpdateUserAndAcbRequest updateRequest) 
									throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {
		
		if(updateRequest.getAcbId() == null || updateRequest.getUserId() == null || updateRequest.getUserId() <= 0 || updateRequest.getAuthority() == null) {
			throw new InvalidArgumentsException("ACB ID, User ID (greater than 0), and Authority are required.");
		}
		
		UserDTO user = userManager.getById(updateRequest.getUserId());
		CertificationBodyDTO acb = acbManager.getById(updateRequest.getAcbId());
		
		if(user == null || acb == null) {
			throw new InvalidArgumentsException("Could not find either ACB or User specified");
		}

		Permission permission = CertificationBodyPermission.toPermission(updateRequest.getAuthority());
		acbManager.addPermission(acb, updateRequest.getUserId(), permission);
		return "{\"userAdded\" : true }";
	}
	
	@RequestMapping(value="/delete_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String deleteUserFromAcb(@RequestBody UpdateUserAndAcbRequest updateRequest) 
								throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException{
		
		if(updateRequest.getAcbId() == null || updateRequest.getUserId() == null || updateRequest.getUserId() <= 0) {
			throw new InvalidArgumentsException("ACB ID and User ID (greater than 0) are required.");
		}
		
		UserDTO user = userManager.getById(updateRequest.getUserId());
		CertificationBodyDTO acb = acbManager.getById(updateRequest.getAcbId());
		
		if(user == null || acb == null) {
			throw new InvalidArgumentsException("Could not find either ACB or User specified");
		}
		
		if(updateRequest.getAuthority() == null) {
			//delete all permissions on that acb
			acbManager.deleteAllPermissionsOnAcb(acb, new PrincipalSid(user.getSubjectName()));
		} else {
			Permission permission = CertificationBodyPermission.toPermission(updateRequest.getAuthority());
			acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), permission);
		}
		return "{\"userDeleted\" : true }";
	}
	
	@RequestMapping(value="/{acbId}/users", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationBodyUserResults getUsers(@PathVariable("acbId") Long acbId) throws InvalidArgumentsException, EntityRetrievalException {
		CertificationBodyDTO acb = acbManager.getById(acbId);
		if(acb == null) {
			throw new InvalidArgumentsException("Could not find the ACB specified.");
		}
		
		List<CertificationBodyUser> acbUsers = new ArrayList<CertificationBodyUser>();
		List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
		for(UserDTO user : users) {
			
			//only show users that have ROLE_ACB_*
			Set<UserPermissionDTO> systemPermissions = userManager.getGrantedPermissionsForUser(user);
			boolean hasAcbPermission = false;
			for(UserPermissionDTO systemPermission : systemPermissions) {
				if(systemPermission.getAuthority().startsWith("ROLE_ACB_")) {
					hasAcbPermission = true;
				}
			}
			
			if(hasAcbPermission) {
				List<String> roleNames = new ArrayList<String>();
				for(UserPermissionDTO role : systemPermissions) {
					roleNames.add(role.getAuthority());
				}
				
				List<Permission> permissions = acbManager.getPermissionsForUser(acb, new PrincipalSid(user.getSubjectName()));
				List<String> acbPerm = new ArrayList<String>(permissions.size());
				for(Permission permission : permissions) {
					CertificationBodyPermission perm = CertificationBodyPermission.fromPermission(permission);
					if(perm != null) {
						acbPerm.add(perm.toString());
					}
				}
				
				CertificationBodyUser userInfo = new CertificationBodyUser();
				userInfo.setUser(new User(user));
				userInfo.setPermissions(acbPerm);
				userInfo.setRoles(roleNames);
				acbUsers.add(userInfo);
			}
		}
		
		CertificationBodyUserResults results = new CertificationBodyUserResults();
		results.setUsers(acbUsers);
		return results;
	}
}
