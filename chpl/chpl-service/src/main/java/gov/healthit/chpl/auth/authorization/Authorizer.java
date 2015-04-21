package gov.healthit.chpl.auth.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.authentication.Authenticator;


@Service("authorizerService")
public class Authorizer {
	
	
	
	
	
	/*
	public static boolean hasGroup(String group){
		
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		//Collection<GrantedAuthority> authorities = currentUser.getAuthorities();
		
	}
	*/
	
	//public static boolean authorize(User user, String claimName, String claimValue){
		//List<Claims> claims = user.getClaims().get(claimName);
		//return (values.indexOf(claimValue) > -1);
	//}
	
}
