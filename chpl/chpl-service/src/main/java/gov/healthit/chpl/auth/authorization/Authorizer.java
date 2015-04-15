package gov.healthit.chpl.auth.authorization;

import java.util.List;

import gov.healthit.chpl.auth.User;

public class Authorizer {
	
	public static boolean authorize(User user, String claimName, String claimValue){
		List<String> values = user.getClaims().get(claimName);
		return (values.indexOf(claimValue) > -1);
	}
}
