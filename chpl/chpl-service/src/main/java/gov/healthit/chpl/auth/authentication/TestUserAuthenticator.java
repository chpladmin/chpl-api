package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.jwt.JWTAuthor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;

public class TestUserAuthenticator implements Authenticator {

	
	private String userID = "testuser";
	private Map<String, List<String>> claims = 
			new HashMap<String, List<String>>() {{
			    put("Groups",  Arrays.asList("ACB123"));
			    put("Roles", Arrays.asList("admin","team_lead","farmer")) ;
			    put("Other",   Arrays.asList("1","2","3") );
			}};
	
	User user = new UserImpl(userID, claims);
	
	@Autowired
	JWTAuthor jwtAuthor;
	
	
	@Override
	public User getUser(LoginCredentials credentials) throws BadCredentialsException {
		
		if  ((credentials.getUserName() == "testuser") && (credentials.getPassword() == "pass" )){
			return user;
		} else {
			throw new BadCredentialsException();
		}
	}
	
	public String getJWT(User user) throws JWTCreationException {
		
		String jwt = null;
		
		try {
			jwt = jwtAuthor.createJWT(user.getSubjectName(), user.getClaims());
		} catch (JoseException e) {
			throw new JWTCreationException(e);
		}
		return jwt;
	}
	
	public String getJWT(LoginCredentials credentials) throws JWTCreationException {
		
		User user;
		try {
			user = getUser(credentials);
		} catch (BadCredentialsException e1) {
			e1.printStackTrace();
		}
		
	}
}
