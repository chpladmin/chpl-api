package gov.healthit.chpl.auth.controller;


import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.UserDTO;
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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
	private static final Logger logger = LogManager.getLogger(UserManagementController.class);

	
	@RequestMapping(value="/create_user_with_roles", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User createUserWithRoles(@RequestBody UserCreationWithRolesJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		UserDTO newUser = userManager.create(userInfo);
		if(userInfo.getRoles() != null && userInfo.getRoles().size() > 0) {
			for(String roleName : userInfo.getRoles()) {
				try {
					userManager.grantRole(newUser.getName(), roleName);
				} catch(UserPermissionRetrievalException ex) {
					logger.error("Could not add role " + roleName + " for user " + newUser.getName(), ex);
				} catch(UserManagementException mex) {
					logger.error("Could not add role " + roleName + " for user " + newUser.getName(), mex);
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
	public User updateUserDetails(@RequestBody UserInfoJSONObject userInfo) throws UserRetrievalException, UserPermissionRetrievalException {

		UserDTO updated = userManager.update(userInfo);
		return new User(updated);
	}
	
	
	@RequestMapping(value="/delete_user", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteUser(@RequestParam("userId") Long userId) 
			throws UserRetrievalException {
		UserDTO toDelete = new UserDTO();
		toDelete.setId(userId);
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
	public String grantUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		userManager.grantRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
		isSuccess = String.valueOf(true);
		
		return "{\"roleAdded\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/revoke_user_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String revokeUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
		isSuccess = String.valueOf(true);
		
		return "{\"roleRemoved\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/list_users", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserListJSONObject getUsers(){
		
		List<UserDTO> userList = userManager.getAll();
		List<UserInfoJSONObject> userInfos = new ArrayList<UserInfoJSONObject>();
		
		for (UserDTO user : userList){
			userInfos.add(new UserInfoJSONObject(user));
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
	
	
}
