package gov.healthit.chpl.auth.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManager;

public class UserAuthenticator extends BaseUserAuthenticator {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public User getUser(LoginCredentials credentials) throws BadCredentialsException {
		
		User user = userManager.getByUserName(credentials.getUserName());
		
		if (checkPassword(credentials.getPassword(), user.getPassword())){
			return user;
		} else {
			throw new BadCredentialsException();
		}
	}

	private boolean checkPassword(String rawPassword, String encodedPassword){
		return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}
	
}
