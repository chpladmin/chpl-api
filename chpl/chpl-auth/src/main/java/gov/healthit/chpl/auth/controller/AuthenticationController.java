package gov.healthit.chpl.auth.controller;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.json.UserResetPasswordJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
	private Authenticator authenticator;
	
	@Autowired
	private UserManager userManager;
	
	//TODO: Create emergency "BUMP TOKENS" method which invalidates all active tokens.
	
	@RequestMapping(value="/authenticate", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authenticateJSON(@RequestBody LoginCredentials credentials) throws JWTCreationException {
		
		String jwt = null;
		jwt = authenticator.getJWT(credentials);
		String jwtJSON = "{\"token\": \""+jwt+"\"}";
		
		return jwtJSON;
	}
	
	@RequestMapping(value="/keep_alive", method= RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public String keepAlive() throws JWTCreationException {
		
		String jwt = authenticator.refreshJWT();
		
		String jwtJSON = "{\"token\": \""+jwt+"\"}";
		
		return jwtJSON;
	}
	
	@RequestMapping(value="/change_password", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String changePassword(@RequestBody LoginCredentials info) throws UserRetrievalException {
		
		userManager.updateUserPassword(info.getUserName(), info.getPassword());
		
		return "{\"passwordUpdated\" : true }";
	}
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody UserResetPasswordJSONObject userInfo) 
			throws UserRetrievalException, MessagingException {		

		String newPassword = userManager.resetUserPassword(userInfo.getUserName(), userInfo.getEmail());
		
		String htmlMessage = "<h3>Your CHPL Password Has Been Reset</h3>"
       			+ "<p>Your new password is</p>" 
       			+ "<pre>" + newPassword + "</pre></br>" 
       			+ "<p>Click the link below to login to your account."
       			+ "<br/>http://localhost:8000/app" + 
       			"</p>";
		//SendMailUtil emailUtils = new SendMailUtil();
		//emailUtils.sendEmail(userInfo.getEmail(), htmlMessage);
		
		return "{\"passwordReset\" : true }";
	
	}
}
