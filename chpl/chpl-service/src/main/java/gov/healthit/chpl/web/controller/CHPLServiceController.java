package gov.healthit.chpl.web.controller;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTCreationException;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.interceptor.Bean;
import gov.healthit.chpl.auth.interceptor.CheckAuthorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CHPLServiceController {
	
	@Autowired
	private Bean bean;
	
	@Autowired
	private Authenticator authenticator;
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String hello(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@CheckAuthorization
	@RequestMapping(value="/authhello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String authHello(@RequestHeader(value="Token") String jwt, @PathVariable String firstName, @PathVariable String lastName) {
		
		
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@RequestMapping(value="/authenticate_json", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authenticateJSON(@RequestBody LoginCredentials credentials) {
		
		System.out.println(credentials.getUserName()+" logged in with password "+credentials.getPassword());
		
		String jwt = null;
		try {
			jwt = authenticator.getJWT(credentials);
		} catch (JWTCreationException e) {
			e.printStackTrace();
		}
		return jwt;
	}
	
	
	@RequestMapping(value="/authenticate", method= RequestMethod.POST, 
			headers = {"content-type=application/x-www-form-urlencoded"},
			produces="application/json; charset=utf-8")
	public String authenticate(@RequestParam("userName") String userName, @RequestParam("password") String password) {
		
		LoginCredentials credentials = new LoginCredentials(userName, password);
		
		System.out.println(credentials.getUserName()+" logged in with password "+credentials.getPassword());
		
		String jwt = null;
		try {
			jwt = authenticator.getJWT(credentials);
		} catch (JWTCreationException e) {
			e.printStackTrace();
		}
		return jwt;
	}
	
	public Bean getBean(){
		return bean;
	}
	
	public void setBean(Bean bn){
		this.bean = bn;
	}
	
	
}
