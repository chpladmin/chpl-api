package gov.healthit.chpl.auth.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Service
public class UserAuthenticator extends BaseUserAuthenticator {

	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserDetailsChecker userDetailsChecker;
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public User getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException {
		
		User user = userManager.getByUserName(credentials.getUserName());
		
		System.out.println(credentials.getPassword());
		System.out.println(user.getPassword());
		System.out.println(checkPassword(credentials.getPassword(), user.getPassword()));
		
		
		if (user != null){
			if (checkPassword(credentials.getPassword(), user.getPassword())){
				
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

	private boolean checkPassword(String rawPassword, String encodedPassword){
		return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}
	
}
