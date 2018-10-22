package gov.healthit.chpl.auth.controller;

import java.util.ArrayList;
import java.util.Arrays;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.auth.EmailBuilder;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.UserResetPasswordJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UpdatePasswordRequest;
import gov.healthit.chpl.auth.user.UpdatePasswordResponse;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * CHPL Authentication controller.
 * @author alarned
 *
 */
@Api(value = "auth")
@RestController
@RequestMapping("/auth")
public class AuthenticationController{
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationController.class);

	@Autowired
	private Authenticator authenticator;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired private Environment env;
	
	//TODO: Create emergency "BUMP TOKENS" method which invalidates all active tokens.
	
	@ApiOperation(value="Log in.", 
			notes="Call this method to authenticate a user. The value returned is that user's "
					+ "token which must be passed on all subsequent requests in the Authorization header. "
					+ "Specifically, the Authorization header must have a value of 'Bearer token-that-gets-returned'.")
	@RequestMapping(value="/authenticate", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authenticateJSON(@RequestBody LoginCredentials credentials) throws JWTCreationException {
		
		String jwt = null;
		jwt = authenticator.getJWT(credentials);
		String jwtJSON = "{\"token\": \""+jwt+"\"}";
		
		return jwtJSON;
	}
	
	@ApiIgnore
	@RequestMapping(value="/keep_alive", method= RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public String keepAlive() throws JWTCreationException {
		
		String jwt = authenticator.refreshJWT();
		
		String jwtJSON = "{\"token\": \""+jwt+"\"}";
		
		return jwtJSON;
	}
	
	@ApiOperation(value="Change password.", 
			notes="Change the logged in user's password as long as the old password "
					+ "passed in matches what is stored in the database.")
	@RequestMapping(value="/change_password", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public UpdatePasswordResponse changePassword(@RequestBody final UpdatePasswordRequest request)
            throws UserRetrievalException {
        UpdatePasswordResponse response = new UpdatePasswordResponse();
        if (Util.getCurrentUser() == null) {
            throw new UserRetrievalException("No user is logged in.");
        }

        // get the current user
        UserDTO currUser = userManager.getById(Util.getCurrentUser().getId());
        if (currUser == null) {
            throw new UserRetrievalException("The user with id " + Util.getCurrentUser().getId()
                    + " could not be found or the logged in user does not have permission to modify their data.");
        }

        // check the strength of the new password
        Strength strength = userManager.getPasswordStrength(currUser, request.getNewPassword());
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(),
                    strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(),
                    strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            response.setStrength(strength);
            response.setPasswordUpdated(false);
            return response;
        }

        // encode the old password passed in to compare
        String currEncodedPassword = userManager.getEncodedPassword(currUser);
        boolean oldPasswordMatches = bCryptPasswordEncoder.matches(request.getOldPassword(), currEncodedPassword);
        if (!oldPasswordMatches) {
            throw new UserRetrievalException("The provided old password does not match the database.");
        } else {
            userManager.updateUserPassword(currUser.getSubjectName(), request.getNewPassword());
        }
        response.setPasswordUpdated(true);
        return response;
    }

	
	@ApiOperation(value="Reset a user's password.", 
			notes="This service generates a new password, saves it to the user's account "
					+ " and sends an email to the address associated with the user's account "
					+ " containing the new password.")
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
		String[] toEmails = {userInfo.getEmail()};

		EmailBuilder emailBuilder = new EmailBuilder(env);
		emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
		                .subject("Open Data CHPL Password Reset")
		                .htmlMessage(htmlMessage)
		                .sendEmail();
		
		return "{\"passwordReset\" : true }";
	
	}
}
