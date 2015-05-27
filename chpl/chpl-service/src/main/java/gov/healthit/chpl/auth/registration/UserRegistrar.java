package gov.healthit.chpl.auth.registration;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRegistrar {
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public boolean createUser(UserDTO userInfo) throws UserCreationException {
		
		User user = null;
		try {
			user = userManager.getByUserName(userInfo.getUserName());
		} catch (UserRetrievalException e) {
			throw new UserCreationException(e);
		}
		
		if (user != null) {
			throw new UserCreationException("user: "+userInfo.getUserName() +" already exists.");
		} else {
			
			String encodedPassword = bCryptPasswordEncoder.encode(userInfo.getPassword());
			UserImpl userToCreate = new UserImpl(userInfo.getUserName(), encodedPassword);
			userManager.create(userToCreate);
			return true;
		}
		
	}

}
