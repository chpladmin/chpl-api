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
public class AuthenticationController {

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private SendMailUtil sendMailService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserManager userManager;

    @Autowired private Environment env;

    //TODO: Create emergency "BUMP TOKENS" method which invalidates all active tokens.

    /**
     * Log a user in.
     * @param credentials user's credentials
     * @return a JSON encoded JWT
     * @throws JWTCreationException if JWT creation fails
     */
    @ApiOperation(value = "Log in.",
            notes = "Call this method to authenticate a user. The value returned is that user's "
                    + "token which must be passed on all subsequent requests in the Authorization header. "
                    + "Specifically, the Authorization header must have a value of 'Bearer token-that-gets-returned'.")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public String authenticateJSON(@RequestBody final LoginCredentials credentials) throws JWTCreationException {

        String jwt = null;
        jwt = authenticator.getJWT(credentials);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";

        return jwtJSON;
    }

    /**
     * Updates a user's JWT with a new one, extending the length of the viability of their session.
     * @return the new JWT
     * @throws JWTCreationException if the JWT cannot be created
     */
    @ApiIgnore
    @RequestMapping(value = "/keep_alive", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public String keepAlive() throws JWTCreationException {

        String jwt = authenticator.refreshJWT();

        String jwtJSON = "{\"token\": \"" + jwt + "\"}";

        return jwtJSON;
    }

    /**
     * Change a logged in user's password.
     * @param request the update password request
     * @return "true" when password is changed
     * @throws UserRetrievalException if user cannot be retrieved or does not have permission to modify their data
     */
    @ApiOperation(value = "Change password.",
            notes = "Change the logged in user's password as long as the old password "
                    + "passed in matches what is stored in the database.")
    @RequestMapping(value = "/change_password", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public String changePassword(@RequestBody final UpdatePasswordRequest request) throws UserRetrievalException {
        if (Util.getCurrentUser() == null) {
            throw new UserRetrievalException("No user is logged in.");
        }
        //get the current user
        UserDTO currUser = userManager.getById(Util.getCurrentUser().getId());
        if (currUser == null) {
            throw new UserRetrievalException("The user with id " + Util.getCurrentUser().getId()
                    + " could not be found or the logged in user does not have permission to modify their data.");
        }

        //encode the old password passed in to compare
        String currEncodedPassword = userManager.getEncodedPassword(currUser);
        boolean oldPasswordMatches = bCryptPasswordEncoder.matches(request.getOldPassword(), currEncodedPassword);
        if (!oldPasswordMatches) {
            throw new UserRetrievalException("The provided old password does not match the database.");
        } else {
            userManager.updateUserPassword(currUser.getSubjectName(), request.getNewPassword());
        }
        return "{\"passwordUpdated\" : true }";
    }

    /**
     * Reset a user's password and email them with the new one.
     * @param userInfo the user whose password needs a reset
     * @return "true" when password is reset
     * @throws UserRetrievalException if user cannot be retrieved
     * @throws MessagingException if email cannot be sent
     */
    @ApiOperation(value = "Reset a user's password.",
            notes = "This service generates a new password, saves it to the user's account "
                    + " and sends an email to the address associated with the user's account "
                    + " containing the new password.")
    @RequestMapping(value = "/reset_password", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public String resetPassword(@RequestBody final UserResetPasswordJSONObject userInfo)
            throws UserRetrievalException, MessagingException {

        String newPassword = userManager.resetUserPassword(userInfo.getUserName(), userInfo.getEmail());

        String htmlMessage = "<p>Hi, <br/>"
                + "Your CHPL account password has been reset. Your new password is: </p>"
                + "<pre>" + newPassword + "</pre>"
                + "<p>Click the link below to login to your account."
                + "<br/>"
                + env.getProperty("chplUrlBegin") + "/#/admin"
                + "</p>"
                + "<p>Take care,<br/> "
                + "The Open Data CHPL Team</p>";
        String[] toEmails = {userInfo.getEmail()};
        sendMailService.sendEmail(toEmails, null, "Open Data CHPL Password Reset", htmlMessage);

        return "{\"passwordReset\" : true }";

    }
}
