package gov.healthit.chpl.auth.controller;


import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.json.GrantAdminJSONObject;
import gov.healthit.chpl.auth.json.GrantRoleJSONObject;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserListJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

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
	
	@Autowired
	UserManager userManager;
	
	@RequestMapping(value="/create_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String createUser(@RequestBody UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		userManager.create(userInfo);
		String isSuccess = String.valueOf(true);
		return "{\"userCreated\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/update_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String updateUserDetails(@RequestBody UserInfoJSONObject userInfo) throws UserRetrievalException, UserPermissionRetrievalException {
		
		userManager.update(userInfo);
		return "{\"userUpdated\" : true }";
		
	}
	
	
	@RequestMapping(value="/delete_user", method= RequestMethod.DELETE,
			produces="application/json; charset=utf-8")
	public String deleteUser(@RequestParam("userName") String userName) 
			throws UserRetrievalException {
		
		userManager.delete(userName);
		return "{\"deletedUser\" : true }";
	}
	
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody LoginCredentials newCredentials) throws UserRetrievalException {
		
		userManager.updateUserPassword(newCredentials.getPassword(), newCredentials.getPassword());
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
	
	@RequestMapping(value="/grant_user_admin", method=RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserAdmin(@RequestBody GrantAdminJSONObject grantAdminObj) 
			throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		userManager.grantAdmin(grantAdminObj.getSubjectName());
		isSuccess = String.valueOf(true);
		
		return "{\"grantedAdminPrivileges\" : "+isSuccess+" }";
		
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
	public @ResponseBody UserInfoJSONObject getUser(@RequestParam("userName") String userName) throws UserRetrievalException{
		
		return userManager.getUserInfo(userName);
		
	}
	
	
}
