package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.GrantRoleJSONObject;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserCreationWithRolesJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserListJSONObject;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserManagementController {
	
	@Autowired UserManager userManager;
	@Autowired CertificationBodyManager acbManager;
	private static final Logger logger = LogManager.getLogger(UserManagementController.class);

	
	@RequestMapping(value="/create_user_with_roles", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User createUserWithRoles(@RequestBody UserCreationWithRolesJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		UserDTO newUser = userManager.create(userInfo);
		
		boolean isChplAdmin = false;
		if(userInfo.getRoles() != null && userInfo.getRoles().size() > 0) {
			for(String roleName : userInfo.getRoles()) {
				try {
					if(roleName.equals("ROLE_ADMIN")) {
						userManager.grantAdmin(newUser.getName());
						isChplAdmin = true;
					} else {
						userManager.grantRole(newUser.getName(), roleName);
					}
				} catch(UserPermissionRetrievalException ex) {
					logger.error("Could not add role " + roleName + " for user " + newUser.getName(), ex);
				} catch(UserManagementException mex) {
					logger.error("Could not add role " + roleName + " for user " + newUser.getName(), mex);
				}
			}
			
			//if they are a chpladmin then they need to be given access to all of the ACBs
			if(isChplAdmin) {
				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.addPermission(acb, newUser.getId(), BasePermission.ADMINISTRATION);
				}
			}
		}
		return new User(newUser);
	}
	
	@RequestMapping(value="/create_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User createUser(@RequestBody UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		UserDTO newUser = userManager.create(userInfo);
		return new User(newUser);
	}
	
	
	@RequestMapping(value="/update_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User updateUserDetails(@RequestBody User userInfo) throws UserRetrievalException, UserPermissionRetrievalException {
		if(userInfo.getUserId() <= 0) {
			throw new UserRetrievalException("Cannot update user with ID less than 0");
		}
		UserDTO updated = userManager.update(userInfo);
		return new User(updated);
	}
	
	
	@RequestMapping(value="/delete_user/{userId}", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteUser(@PathVariable("userId") Long userId) 
			throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		if(userId <= 0) {
			throw new UserRetrievalException("Cannot delete user with ID less than 0");
		}
		UserDTO toDelete = userManager.getById(userId);
		
		//delete the acb permissions for that user
		acbManager.deletePermissionsForUser(toDelete);
		
		//delete the user
		userManager.delete(toDelete);
		
		return "{\"deletedUser\" : true }";
	}
	
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody LoginCredentials newCredentials) throws UserRetrievalException {
		
		userManager.updateUserPassword(newCredentials.getUserName(), newCredentials.getPassword());
		return "{\"passwordUpdated\" : true }";
	
	}
	
	@RequestMapping(value="/grant_user_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.grantAdmin(user.getSubjectName());

				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.addPermission(acb, user.getId(), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to grant ROLE_ADMIN");
			}
		} else {
			userManager.grantRole(user.getSubjectName(), grantRoleObj.getRole());
		}

		isSuccess = String.valueOf(true);
		return "{\"roleAdded\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/revoke_user_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String revokeUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.removeAdmin(user.getSubjectName());
				
				//if they were a chpladmin then they need to have all ACB access removed
				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else if(grantRoleObj.getRole().equals("ROLE_ACB_ADMIN")) {
			try {
				userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
				
				//if they were an acb admin then they need to have all ACB access removed
				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else {
			userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
		}
		

		
		isSuccess = String.valueOf(true);
		return "{\"roleRemoved\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/list_users", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserListJSONObject getUsers(){
		
		List<UserDTO> userList = userManager.getAll();
		List<UserInfoJSONObject> userInfos = new ArrayList<UserInfoJSONObject>();
		
		for (UserDTO user : userList){
			Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(user);
			
			UserInfoJSONObject userInfo = new UserInfoJSONObject(user);
			List<String> permissionList = new ArrayList<String>(permissions.size());
			for(UserPermissionDTO permission : permissions) {
				permissionList.add(permission.getAuthority());
			}
			userInfo.setRoles(permissionList);
			userInfos.add(userInfo);
		}
		
		UserListJSONObject ulist = new UserListJSONObject();
		ulist.setUsers(userInfos);
		return ulist;
	}
	
	@RequestMapping(value="/user_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserInfoJSONObject getUser(@RequestParam("userName") String userName) throws UserRetrievalException {
		
		return userManager.getUserInfo(userName);
		
	}
	
	@RequestMapping(value="/invite", method=RequestMethod.POST,
			produces="application/json; charset=utf-8") 
	public @ResponseBody String inviteUser(@RequestParam("email") String userEmail) {
		
		return "{succcess: true}";
	}
	
}
