package gov.healthit.chpl.auth.authentication;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Service
public class UserAuthenticator extends BaseUserAuthenticator {
	
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserDetailsChecker userDetailsChecker;
	
	
	@Override
	public UserDTO getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException {
		
		
		Authentication authenticator = new Authentication() {
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
				auths.add(new GrantedPermission("ROLE_USER_AUTHENTICATOR"));
				return auths;
			}

			@Override
			public Object getCredentials(){
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal(){
				return null;
			}
			@Override
			public boolean isAuthenticated(){
				return true;
			}

			@Override
			public void setAuthenticated(boolean arg0) throws IllegalArgumentException {}
			
			@Override
			public String getName(){
				return "AUTHENTICATOR";
			}
			
		};
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		UserDTO user = userManager.getByName(credentials.getUserName());
		SecurityContextHolder.getContext().setAuthentication(null);
		
		
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
