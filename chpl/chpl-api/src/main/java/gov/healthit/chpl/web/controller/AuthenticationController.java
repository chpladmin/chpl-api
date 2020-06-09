package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.ResetPasswordRequest;
import gov.healthit.chpl.domain.auth.UpdateExpiredPasswordRequest;
import gov.healthit.chpl.domain.auth.UpdatePasswordRequest;
import gov.healthit.chpl.domain.auth.UpdatePasswordResponse;
import gov.healthit.chpl.domain.auth.UserResetPasswordRequest;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.EmailBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "auth")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserManager userManager;

    @Autowired
    private JWTUserConverter userConverter;

    @Autowired
    private Environment env;

    // TODO: Create emergency "BUMP TOKENS" method which invalidates all active
    // tokens.

    /**
     * Log in a user.
     * @param credentials the user's credentials
     * @return a JWT with an authentication token
     * @throws JWTCreationException if unable to create the JWT
     * @throws UserRetrievalException if user is required to change their password
     */
    @ApiOperation(value = "Log in.",
            notes = "Call this method to authenticate a user. The value returned is that user's "
                    + "token which must be passed on all subsequent requests in the Authorization header. "
                    + "Specifically, the Authorization header must have a value of 'Bearer token-that-gets-returned'.")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public String authenticateJSON(@RequestBody final LoginCredentials credentials)
            throws JWTCreationException, UserRetrievalException {

        String jwt = authenticationManager.authenticate(credentials);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    /**
     * Update the user's JWT to keep their session alive.
     * @return a new JWT with an extended expiration date
     * @throws JWTCreationException if unable to create the JWT
     * @throws UserRetrievalException if cannot find user to refresh
     */
    @ApiIgnore
    @RequestMapping(value = "/keep_alive", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public String keepAlive() throws JWTCreationException, UserRetrievalException {

        String jwt = authenticationManager.refreshJWT();

        String jwtJSON = "{\"token\": \"" + jwt + "\"}";

        return jwtJSON;
    }

    /**
     * Change a user's password.
     * @param request the request containing old/new passwords
     * @return a confirmation response, or an error iff the user's new password does not meet requirements
     * @throws UserRetrievalException if unable to retrieve the user
     */
    @ApiOperation(value = "Change password.",
            notes = "Change the logged in user's password as long as the old password "
                    + "passed in matches what is stored in the database.")
    @RequestMapping(value = "/change_password", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse changePassword(@RequestBody final UpdatePasswordRequest request)
            throws UserRetrievalException {
        UpdatePasswordResponse response = new UpdatePasswordResponse();
        if (AuthUtil.getCurrentUser() == null) {
            throw new UserRetrievalException("No user is logged in.");
        }

        // get the current user
        UserDTO currUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        if (currUser == null) {
            throw new UserRetrievalException("The user with id " + AuthUtil.getCurrentUser().getId()
                    + " could not be found or the logged in user does not have permission to modify their data.");
        }

        // check the strength of the new password
        Strength strength = userManager.getPasswordStrength(currUser, request.getNewPassword());
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
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

    /**
     * Change a user's expired password.
     * @param request the request containing old/new passwords
     * @return a confirmation response, or an error iff the user's new password does not meet requirements
     * @throws UserRetrievalException if unable to retrieve the user
     * @throws JWTCreationException if cannot create a JWT
     * @throws JWTValidationException if cannot validate JWT
     */
    @ApiOperation(value = "Change expired password.",
            notes = "Change a user's expired password as long as the old password "
                    + "passed in matches what is stored in the database.")
    @RequestMapping(value = "/change_expired_password", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse changeExpiredPassword(@RequestBody final UpdateExpiredPasswordRequest request)
            throws UserRetrievalException, JWTCreationException, JWTValidationException {
        UpdatePasswordResponse response = new UpdatePasswordResponse();

        // get the user trying to change their password
        UserDTO currUser = authenticationManager.getUser(request.getLoginCredentials());
        if (currUser == null) {
            throw new UserRetrievalException("Cannot update password; bad username or password");
        }

        // check the strength of the new password
        Strength strength = userManager.getPasswordStrength(currUser, request.getNewPassword());
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH
                || request.getNewPassword().equals(request.getOldPassword())) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(),
                    strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(),
                    strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            response.setStrength(strength);
            response.setPasswordUpdated(false);
            response.getSuggestions().add("New password cannot match old password");
            return response;
        }

        // encode the old password passed in to compare
        String currEncodedPassword = userManager.getEncodedPassword(currUser);
        boolean oldPasswordMatches = bCryptPasswordEncoder.matches(request.getOldPassword(), currEncodedPassword);
        if (!oldPasswordMatches) {
            throw new UserRetrievalException("The provided old password does not match the database.");
        } else {
            String jwt = authenticationManager.getJWT(currUser);
            User authenticatedUser = userConverter.getAuthenticatedUser(jwt);
            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
            userManager.updateUserPassword(currUser.getSubjectName(), request.getNewPassword());
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        response.setPasswordUpdated(true);
        return response;
    }

    /**
     * Allow the user to reset their password given they have the correct token.
     * @param request the reset request
     * @return the results of their reset
     */
    @ApiOperation(value = "Reset password.", notes = "Reset the users password.")
    @RequestMapping(value = "/reset_password_request", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse resetPassword(@RequestBody final ResetPasswordRequest request)
            throws UserRetrievalException {
        UpdatePasswordResponse response = new UpdatePasswordResponse();
        // get the current user
        UserDTO currUser = userManager.getByNameUnsecured(request.getUserName());
        if (currUser == null) {
            throw new UserRetrievalException("The user with username " + request.getUserName() + " cannot be found.");
        }
        // check the strength of the new password
        Strength strength = userManager.getPasswordStrength(currUser, request.getNewPassword());
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            response.setStrength(strength);
            response.setPasswordUpdated(false);
            return response;
        }
        if (userManager.authorizePasswordReset(request.getToken())) {
            userManager.updateUserPasswordUnsecured(currUser.getSubjectName(), request.getNewPassword());
            userManager.deletePreviousTokens(request.getToken());
            response.setPasswordUpdated(true);
        } else {
            response.setPasswordUpdated(false);
        }
        return response;
    }

    @ApiOperation(value = "Reset a user's password.", notes = "")
    @RequestMapping(value = "/email_reset_password", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String resetPassword(@RequestBody final UserResetPasswordRequest userInfo)
            throws UserRetrievalException, MessagingException {

        UserResetTokenDTO userResetTokenDTO = userManager.createResetUserPasswordToken(userInfo.getUserName(),
                userInfo.getEmail());
        String htmlMessage = "<p>Hi, <br/>" + "Please follow this link to reset your password </p>" + "<pre>"
                + env.getProperty("chplUrlBegin") + "/#/admin/authorizePasswordReset?token="
                + userResetTokenDTO.getUserResetToken() + "</pre>" + "<br/>" + "</p>" + "<p>Take care,<br/> "
                + "The Open Data CHPL Team</p>";
        String[] toEmails = {
                userInfo.getEmail()
        };

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails))).subject("Open Data CHPL Password Reset")
        .htmlMessage(htmlMessage).sendEmail();

        return "{\"passwordResetEmailSent\" : true }";
    }

    @ApiOperation(value = "Impersonate another user.", notes = "")
    @RequestMapping(value = "/impersonate", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public String impersonateUser(@RequestHeader(value = "Authorization", required = true) final String userJwt,
            @RequestParam(value = "username", required = true) final String username)
                    throws UserRetrievalException, JWTCreationException, UserManagementException, JWTValidationException {

        String jwt = authenticationManager.impersonateUser(username);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    @ApiOperation(value = "Stop impersonating another user.", notes = "")
    @RequestMapping(value = "/unimpersonate", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public String unimpersonateUser(@RequestHeader(value = "Authorization", required = true) final String userJwt)
            throws JWTValidationException, JWTCreationException, UserRetrievalException {
        User user = userConverter.getImpersonatingUser(userJwt.split(" ")[1]);
        String jwt = authenticationManager.unimpersonateUser(user);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }
}
