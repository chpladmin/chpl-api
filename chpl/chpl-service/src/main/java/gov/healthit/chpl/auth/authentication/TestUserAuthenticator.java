package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.Claim;
import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.AuthenticatedUser;
import gov.healthit.chpl.auth.jwt.JWTAuthor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TestUserAuthenticator implements Authenticator {

	
	private static String userID = "testuser";
	private static String password = "pass";
	private List<Claim> claims =  Arrays.asList(new Claim("GROUP_ACB123"),new Claim("ROLE_team_lead"),new Claim("ROLE_farmer"));
	User user = new AuthenticatedUser(userID, claims);
	
	@Autowired
	JWTAuthor jwtAuthor;
	
	
	//@Override
	private User getUser(LoginCredentials credentials) throws BadCredentialsException {
		
		if  ((credentials.getUserName().equals(userID)) && (credentials.getPassword().equals(password))){
			return user;
		} else {
			throw new BadCredentialsException();
		}
	}
	
	public String getJWT(User user) throws JWTCreationException {
		
		String jwt = null;
		Map<String, List<String>> claims = new HashMap<String, List<String>>();
		
		List<String> claimStrings = new ArrayList<String, List<String>>();
		
		for (Claim claim : user.getClaims()){
			claimStrings.add(claim.getAuthority);
		}
		claims.put("Authorities", claimStrings);
		
		jwt = jwtAuthor.createJWT(user.getSubjectName(), claimStrings);
		
		return jwt;
		
	}
	
	public String getJWT(LoginCredentials credentials) throws JWTCreationException {
		
		String jwt = null;
		User user = null;
		try {
			user = getUser(credentials);
		} catch (BadCredentialsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (user != null){
			jwt = getJWT(user);
		}
		return jwt;
		
	}
}
