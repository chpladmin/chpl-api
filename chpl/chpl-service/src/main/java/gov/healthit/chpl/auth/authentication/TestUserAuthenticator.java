package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.UserImpl;
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
		
		if  ((credentials.getUserName().equals(userID)) && (credentials.getPassword().equals(password))){
			return user;
		} else {
			throw new BadCredentialsException();
		}
	}
	
	public String getJWT(User user) throws JWTCreationException {
		
		String jwt = null;
		jwt = jwtAuthor.createJWT(user.getSubjectName(), user.getClaims());
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
			jwt = jwtAuthor.createJWT(user.getSubjectName(), user.getClaims());
		}
		return jwt;
		
	}
}
