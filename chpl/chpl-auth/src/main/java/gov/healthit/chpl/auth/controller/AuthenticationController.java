package gov.healthit.chpl.auth.controller;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.UserResetPasswordJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UpdatePasswordRequest;
import gov.healthit.chpl.auth.user.UserConversionHelper;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController{
	
	@Autowired
	private Authenticator authenticator;
	@Autowired SendMailUtil sendMailService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired private Environment env;
	
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
	public String changePassword(@RequestBody UpdatePasswordRequest request) throws UserRetrievalException {
		if(Util.getCurrentUser() == null) {
			throw new UserRetrievalException("No user is logged in.");
		}
		//get the current user
		UserDTO currUser = userManager.getById(Util.getCurrentUser().getId());
		if(currUser == null) {
			throw new UserRetrievalException("The user with id " + Util.getCurrentUser().getId() + " could not be found or "
					+ "the logged in user does not have permission to modify their data.");
		}
		
		//encode the old password passed in to compare
		String currEncodedPassword = userManager.getEncodedPassword(currUser);
		boolean oldPasswordMatches = bCryptPasswordEncoder.matches(request.getOldPassword(), currEncodedPassword);
		if(!oldPasswordMatches) {
			throw new UserRetrievalException("The provided old password does not match the database.");
		} else {
			userManager.updateUserPassword(currUser.getSubjectName(), request.getNewPassword());
		}
		return "{\"passwordUpdated\" : true }";
	}
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody UserResetPasswordJSONObject userInfo) 
			throws UserRetrievalException, MessagingException {		

		String newPassword = userManager.resetUserPassword(userInfo.getUserName(), userInfo.getEmail());

		String htmlMessage = "<p>Hi, <br/>"
       			+ "Your CHPL account password has been reset. Your new password is: </p>"
				+ "<pre>" + newPassword + "</pre>"
       			+ "<p>Click the link below to login to your account."
       			+ "<br/>" +
       			env.getProperty("chplUrlBegin") + "/#/admin" +
       			"</p>"
       			+ "<p>Take care,<br/> " +
				 "The Open Data CHPL Team</p>";
		sendMailService.sendEmail(userInfo.getEmail(), "Open Data CHPL Password Reset", htmlMessage);
		
		return "{\"passwordReset\" : true }";
	
	}
}
