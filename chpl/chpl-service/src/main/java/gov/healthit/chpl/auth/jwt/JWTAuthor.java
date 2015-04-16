package gov.healthit.chpl.auth.jwt;


import gov.healthit.chpl.auth.Claim;

import java.util.List;
import java.util.Map;


public interface JWTAuthor {
	
	public String createJWT(String subject, Map<String, List<Claim> > claims) ;
	
}
