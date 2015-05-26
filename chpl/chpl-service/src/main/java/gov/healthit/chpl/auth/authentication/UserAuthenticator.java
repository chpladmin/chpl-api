package gov.healthit.chpl.auth.authentication;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManager;

public class UserAuthenticator extends BaseUserAuthenticator {

	@Autowired
	private UserManager userManager;
	
	@Override
	public User getUser(LoginCredentials credentials)
			throws BadCredentialsException {
		//AuthenticatedUser user = userManager.
	}

}
