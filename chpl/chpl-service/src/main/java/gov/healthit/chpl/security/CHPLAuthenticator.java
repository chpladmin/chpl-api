package gov.healthit.chpl.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.authentication.UserAuthenticator;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManager;



public class CHPLAuthenticator extends UserAuthenticator {

	@Autowired
	private JWTAuthor jwtAuthor;
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public String getJWT(User user) throws JWTCreationException {
		
		String jwt = null;
		
		Map<String, List<String>> claims = new HashMap<String, List<String>>();
		List<String> roleStrings = new ArrayList<String>();
		
		for (UserPermissionEntity role : user.getPermissions()){
			roleStrings.add(role.getAuthority());
		}
		claims.put("Authorities", roleStrings);
		
		jwt = jwtAuthor.createJWT(user.getSubjectName(), claims);
		return jwt;
		
	}
	
	/*
	private List<UserPermission> getPermissions(User user){
		
	}
	*/
}
