package gov.healthit.chpl.auth.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.user.User;

public abstract class BaseUserAuthenticator implements Authenticator {

	@Autowired
	private JWTAuthor jwtAuthor;

	abstract public User getUser(LoginCredentials credentials)  throws BadCredentialsException;
	
	public String getJWT(User user) throws JWTCreationException {
		
		String jwt = null;
		Map<String, List<String>> claims = new HashMap<String, List<String>>();
		List<String> claimStrings = new ArrayList<String>();
		
		for (Claim claim : user.getClaims()){
			claimStrings.add(claim.getAuthority());
		}
		claims.put("Authorities", claimStrings);
		jwt = jwtAuthor.createJWT(user.getSubjectName(), claims);
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
	
	public JWTAuthor getJwtAuthor() {
		return jwtAuthor;
	}


	public void setJwtAuthor(JWTAuthor jwtAuthor) {
		this.jwtAuthor = jwtAuthor;
	}

}
