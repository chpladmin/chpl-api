package gov.healthit.chpl.auth.authentication;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Service
public class UserAuthenticator extends BaseUserAuthenticator {
	
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserDetailsChecker userDetailsChecker;
	
	
	@Override
	public UserDTO getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException {
		
		UserDTO user = userManager.getByName(credentials.getUserName());
		
		if (user != null){
			if (checkPassword(credentials.getPassword(), userManager.getEncodedPassword(user))){
				
				try {
					userDetailsChecker.check(user);
				} catch (AccountStatusException ex) {
					throw ex;
				}
				return user;
				
			} else {
				throw new BadCredentialsException("Bad username and password combination.");
			}
		} else {
			throw new BadCredentialsException("Bad username and password combination.");
		}
	}

	protected boolean checkPassword(String rawPassword, String encodedPassword){
		return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}
	
}
