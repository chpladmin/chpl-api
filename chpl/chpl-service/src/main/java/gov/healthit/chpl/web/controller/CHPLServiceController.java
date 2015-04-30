package gov.healthit.chpl.web.controller;

import java.util.List;

import gov.healthit.chpl.acb.CertificationBody;
import gov.healthit.chpl.acb.CertificationBodyManager;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTCreationException;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.interceptor.Bean;
import gov.healthit.chpl.auth.interceptor.CheckAuthorization;
import gov.healthit.chpl.auth.interceptor.GlobalAdminOnly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
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
	@Qualifier("acbManager")
	private CertificationBodyManager acbManager;
	
	
	@Autowired
	private Authenticator authenticator;
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String hello(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@PreAuthorize("@authorizerService.isChris(#firstName)")
	@RequestMapping(value="/authhello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String authHello(@PathVariable String firstName, @PathVariable String lastName) {
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";	
	}
	
	
	@RequestMapping(value="/adminACB/{acbID}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String adminACB(@PathVariable String acbID) {
		
		List<CertificationBody> authorizedACBs = acbManager.getAll();
		
		if (authorizedACBs.size() > 0){
			return "{\"AdminsteringACB:\" : \""+acbID+"\"}";
		} else {
			return "{\"AdminsteringACB:\" : \"-1\"}";
		}
	}
	
	
	@GlobalAdminOnly
	@RequestMapping(value="/secrethello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String secretHello(@RequestHeader(value="Token") String jwt, @PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	@CheckAuthorization
	@RequestMapping(value="/hellolist/{group}/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String authHelloList(@RequestHeader(value="Token") String jwt, @PathVariable String firstName, @PathVariable String lastName) {
		
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
