package gov.healthit.chpl.auth.registration;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

public class UserRegistrar {
	
	@Autowired
	private UserManager userManager;
	
	public boolean createUser(UserDTO userInfo){
		
		User user = userManager.getByUserName(userInfo.getUserName());
		
		if (user != null) {
			throw new BadCredentialsException("user: "+userInfo.getUserName() +" already exists.");
		} else {
			
			UserImpl userToCreate = new UserImpl(userInfo.getUserName(), userInfo.getPassword());
			userManager.create(userToCreate);
			return true;
		}
		
	}

}
