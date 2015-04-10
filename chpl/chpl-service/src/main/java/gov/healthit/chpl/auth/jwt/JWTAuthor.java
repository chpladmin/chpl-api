package gov.healthit.chpl.auth.jwt;

import java.util.List;
import java.util.Map;

import org.jose4j.lang.JoseException;

public interface JWTAuthor {
	
	public String createJWT(String subject, Map<String, List<String> > claims)  throws JoseException;
	
}
