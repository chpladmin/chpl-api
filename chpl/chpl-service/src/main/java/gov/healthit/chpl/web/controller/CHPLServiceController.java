package gov.healthit.chpl.web.controller;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.interceptor.Bean;
import gov.healthit.chpl.auth.interceptor.CheckAuthorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CHPLServiceController {
	
	@Autowired
	private Bean bean;
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String hello(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@CheckAuthorization
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String authHello(@RequestHeader(value="JWT") String jwt, @PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@RequestMapping(value="/authenticate",headers = "content-type=multipart/*",  method= RequestMethod.POST, produces="application/json; charset=utf-8")
	public String authenticate(@RequestBody LoginCredentials credentials) {
		
		System.out.println(credentials.getUserName()+" logged in with password"+credentials.getPassword());
		String jwt = 
	}

	
	
	public Bean getBean(){
		return bean;
	}
	
	public void setBean(Bean bn){
		this.bean = bn;
	}
	
	
}
