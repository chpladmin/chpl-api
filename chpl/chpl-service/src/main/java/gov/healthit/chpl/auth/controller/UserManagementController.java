package gov.healthit.chpl.auth.controller;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.jwt.JWTCreationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserManagementController {
	
	@RequestMapping(value="/authenticate_json", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authenticateJSON(@RequestBody LoginCredentials credentials) {
		
		String jwt = null;
		try {
			jwt = authenticator.getJWT(credentials);
		} catch (JWTCreationException e) {
			e.printStackTrace();
		}
		return jwt;
	}
	
	
	
}
