package gov.healthit.chpl.auth.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.UserImpl;
import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.jwt.JWTValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizorImpl implements Authorizor {
	
	@Autowired
	JWTConsumer jwtConsumer;
	
	public User getUser(String jwt) throws JWTValidationException{
		
		User user = null;
		
		Map<String, Object> validatedClaims = jwtConsumer.consume(jwt);
		
		if (validatedClaims == null){
			throw new JWTValidationException();
		} else {
			
			/*
			 * Handle the standard claim types. These won't be lists of Strings,
			 * which we'll be expecting from the claims we are creating ourselves
			 */
			Object issuer = validatedClaims.remove("iss");
			Object audience = validatedClaims.remove("aud");
			Object issuedAt = validatedClaims.remove("iat");
			Object notBefore = validatedClaims.remove("nbf");
			Object expires = validatedClaims.remove("exp");
			Object jti = validatedClaims.remove("jti");
			Object typ = validatedClaims.remove("typ");
			
			String subject = (String) validatedClaims.remove("sub");
			Map<String, List<String>> claims = new HashMap<String, List<String>>();
			
			for (Map.Entry<String, Object> claim : validatedClaims.entrySet())
			{
			    System.out.println(claim.getKey() + "/" + claim.getValue());
			    String key = claim.getKey();
			    List<String> values = (List<String>) claim.getValue();
			    claims.put(key, values);
			    
			}
			user = new UserImpl(subject, claims);
		}
		return user;
	}
}
