package gov.healthit.chpl.auth.controller;


import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.registration.UserCreationException;
import gov.healthit.chpl.auth.user.registration.UserDTO;
import gov.healthit.chpl.auth.user.registration.UserRegistrar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserManagementController {
	
	@Autowired
	UserRegistrar registrar;
	
	@Autowired
	UserManager userManager;
	
	@RequestMapping(value="/create_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String createUser(@RequestBody UserDTO userInfo) throws UserCreationException {
		
		registrar.createUser(userInfo);
		String isSuccess = String.valueOf(true);
		return "{\"userCreated\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/deactivate_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces="application/json; charset=utf-8")
	public String deactivateUser(@RequestParam("userName") String userName) 
			throws UserRetrievalException {
		
		String isSuccess = String.valueOf(registrar.deactivateUser(userName));
		return "{\"deactivatedUser\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestParam("userName") String userName, 
			@RequestParam("password") String password) throws UserRetrievalException {
		
		boolean passwordUpdated = registrar.updateUserPassword(userName, password);
		String isSuccess = String.valueOf(passwordUpdated);
		return "{\"passwordUpdated\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/update_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String updateUserDetails(@RequestBody UserDTO userInfo) throws UserRetrievalException {
		//TODO: Build out UserDTO to contain eg: Contacts
		
		UserImpl user = new UserImpl(userInfo.getUserName(), registrar.getEncodedPassword(userInfo.getPassword()));
		userManager.create(user);
		return "{\"userUpdated\" : true }";
		
	}
	
	
	@RequestMapping(value="/grant_user_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserRole(@RequestParam("userName") String userName, 
			@RequestParam("role") String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		
		User fetchedUser = userManager.getByUserName(userName);
		String isSuccess = String.valueOf(false);
		
		if (fetchedUser == null){
			throw new UserRetrievalException("User not found");
		} else {
			UserImpl user = (UserImpl) fetchedUser;
			userManager.grantRole(user, role);
			isSuccess = String.valueOf(true);
		}
		
		return "{\"roleAdded\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/grant_user_admin", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserAdmin(@RequestParam("userName") String userName) 
			throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		User fetchedUser = userManager.getByUserName(userName);
		String isSuccess = String.valueOf(false);
		
		if (fetchedUser == null){
			throw new UserRetrievalException("User not found");
		} else {
			UserImpl user = (UserImpl) fetchedUser;
			userManager.grantAdmin(user);
			isSuccess = String.valueOf(true);
		}
		
		return "{\"grantedAdmin\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/init_admin", method= RequestMethod.GET)
	public void initAdminUser() {
		registrar.createAdminUser();
	}
	
}
