package gov.healthit.chpl.auth.controller;


import gov.healthit.chpl.auth.user.UserImpl;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
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
	
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody UserDTO userInfo) throws UserRetrievalException {
		
		UserImpl user = (UserImpl) userManager.getByUserName(userInfo.getUserName());
		user.setPassword(userInfo.getUserName());
		
		userManager.update(user);
		
		String isSuccess = String.valueOf(true);
		return "{\"passwordReset\" : "+isSuccess+" }";
		
	}
	
	
	@RequestMapping(value="/update_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String updateUserDetails(@RequestBody UserDTO userInfo) throws UserRetrievalException {
		return "";
	}
	
	@RequestMapping(value="/update_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String addUserRole(@RequestBody UserDTO userInfo, String role) throws UserRetrievalException {
		return "";
	}
	
	
}
