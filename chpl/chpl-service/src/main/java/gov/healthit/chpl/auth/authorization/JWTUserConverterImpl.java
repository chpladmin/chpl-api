package gov.healthit.chpl.auth.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.AuthenticatedUser;
import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.jwt.JWTValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class JWTUserConverterImpl implements JWTUserConverter {
	
	@Autowired
	JWTConsumer jwtConsumer;
	
	public JWTUserConverterImpl(){}
	
	public User getUser(String jwt) throws JWTValidationException {
		
		User user = new AuthenticatedUser();
		
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
			user.setSubjectName(subject);
			
			List<String> claims = new ArrayList<String>();
			
			for (Map.Entry<String, Object> claim : validatedClaims.entrySet())
			{
			    List<String> values = (List<String>) claim.getValue();
			    claims.addAll(values);
			}
			
			for (String claimValue : claims){
				user.addClaim(claimValue);
			}
			
		}
		return user;
	}
}
