package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.Claim;
import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.AuthenticatedUser;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;


@Service
public class TestUserAuthenticator extends BaseUserAuthenticator {

	
	private static String userID = "testuser";
	private static String password = "pass";
	private List<Claim> claims =  Arrays.asList(new Claim("GROUP_ACB123"),new Claim("ROLE_team_lead"),new Claim("ROLE_farmer"));
	User user = new AuthenticatedUser(userID, claims);
	
	@Override
	public User getUser(LoginCredentials credentials) throws BadCredentialsException {
		
		if  ((credentials.getUserName().equals(userID)) && (credentials.getPassword().equals(password))){
			return user;
		} else {
			throw new BadCredentialsException();
		}
	}
	
}
