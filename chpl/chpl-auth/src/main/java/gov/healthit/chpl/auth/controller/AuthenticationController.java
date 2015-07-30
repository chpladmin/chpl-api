package gov.healthit.chpl.auth.controller;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.jwt.JWTCreationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
	private Authenticator authenticator;
	
	
	//TODO: Create emergency "BUMP TOKENS" method which invalidates all active tokens.
	
	@RequestMapping(value="/authenticate", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authenticateJSON(@RequestBody LoginCredentials credentials) throws JWTCreationException {
		
		String jwt = null;
		jwt = authenticator.getJWT(credentials);
		String jwtJSON = "{\"token\" : "+jwt+" }";
		
		return jwtJSON;
	}
	
	@RequestMapping(value="/authenticate_form", method= RequestMethod.POST, 
			headers = {"content-type=application/x-www-form-urlencoded"},
			produces="application/json; charset=utf-8")
	public String authenticate(@RequestParam("userName") String userName, @RequestParam("password") String password) throws JWTCreationException {
		
		LoginCredentials credentials = new LoginCredentials(userName, password);
		
		String jwt = null;
		jwt = authenticator.getJWT(credentials);
		String jwtJSON = "{\"token\" : "+jwt+" }";
		return jwtJSON;
	}
	
}
