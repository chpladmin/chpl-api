package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.ChplAccountStatusException;
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
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "auth", description = "User authentication operations including login.")
@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthenticationController {
    private AuthenticationManager authenticationManager;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserManager userManager;
    private JWTUserConverter userConverter;
    private Environment env;
    private ErrorMessageUtil msgUtil;
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, BCryptPasswordEncoder bCryptPasswordEncoder,
            UserManager userManager, JWTUserConverter userConverter, ErrorMessageUtil msgUtil,
            Environment env, ChplEmailFactory chplEmailFactory) {
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userManager = userManager;
        this.userConverter = userConverter;
        this.msgUtil = msgUtil;
        this.env = env;
        this.chplEmailFactory = chplEmailFactory;
    }

    @Operation(summary = "Log in.",
            description = "Call this method to authenticate a user. The value returned is that user's "
                    + "token which must be passed on all subsequent requests in the Authorization header. "
                    + "Specifically, the Authorization header must have a value of 'Bearer token-that-gets-returned'.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @ApiResponse(responseCode = "461", description = "The confirmation email has been resent to the user.")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String authenticateJSON(@RequestBody LoginCredentials credentials)
            throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException,
            ChplAccountEmailNotConfirmedException {

        String jwt = authenticationManager.authenticate(credentials);
        if (jwt == null) {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    @Hidden
    @RequestMapping(value = "/keep_alive", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public String keepAlive() throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException {
        String jwt = authenticationManager.refreshJWT();
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    @Operation(summary = "Change password.",
            description = "Change the logged in user's password as long as the old password "
                    + "passed in matches what is stored in the database.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/change_password", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse changePassword(@RequestBody UpdatePasswordRequest request)
            throws UserRetrievalException, MultipleUserAccountsException {
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
            userManager.updateUserPassword(userManager.getByNameOrEmail(currUser.getEmail()), request.getNewPassword());
        }
        response.setPasswordUpdated(true);
        return response;
    }

    @Operation(summary = "Change expired password.",
            description = "Change a user's expired password as long as the old password "
                    + "passed in matches what is stored in the database.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/change_expired_password", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse changeExpiredPassword(@RequestBody UpdateExpiredPasswordRequest request)
            throws UserRetrievalException, JWTCreationException, JWTValidationException,
            MultipleUserAccountsException, AccountStatusException, ChplAccountEmailNotConfirmedException {
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
            userManager.updateUserPasswordUnsecured(currUser.getEmail(), request.getNewPassword());
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        response.setPasswordUpdated(true);
        return response;
    }

    @Operation(summary = "Reset password.", description = "Reset the user's password.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/reset_password_request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse resetPassword(@RequestBody ResetPasswordRequest request)
            throws UserRetrievalException, MultipleUserAccountsException {
        return userManager.authorizePasswordReset(request);
    }

    @Operation(summary = "Reset a user's password.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/email_reset_password", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String resetPassword(@RequestBody UserResetPasswordRequest userInfo)
            throws UserRetrievalException, EmailNotSentException {

        UserResetTokenDTO userResetTokenDTO = userManager.createResetUserPasswordToken(userInfo.getEmail());
        String htmlMessage = String.format(env.getProperty("user.resetPassword.body"),
                env.getProperty("chplUrlBegin"), userResetTokenDTO.getUserResetToken());
        String[] toEmails = {
                userInfo.getEmail()
        };

        chplEmailFactory.emailBuilder().recipients(new ArrayList<String>(Arrays.asList(toEmails)))
                .subject(env.getProperty("user.resetPassword.subject"))
                .htmlMessage(htmlMessage)
                .publicHtmlFooter()
                .sendEmail();

        return "{\"passwordResetEmailSent\" : true }";
    }

    @Deprecated
    @Operation(summary = "DEPRECATED. Impersonate another user.", description = "",
            deprecated = true,
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/impersonate", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String impersonateUser(@RequestHeader(value = "Authorization", required = true) String userJwt,
            @RequestParam(value = "username", required = true) String username)
            throws UserRetrievalException, JWTCreationException, UserManagementException, JWTValidationException {

        String jwt = authenticationManager.impersonateUserByUsername(username);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    @Operation(summary = "Impersonate another user.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/beta/impersonate", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String impersonateUserById(@RequestHeader(value = "Authorization", required = true) String userJwt,
            @RequestParam(value = "id", required = true) Long id)
            throws UserRetrievalException, JWTCreationException, UserManagementException,
            JWTValidationException, MultipleUserAccountsException {

        String jwt = authenticationManager.impersonateUser(id);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }

    @Operation(summary = "Stop impersonating another user.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/unimpersonate", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String unimpersonateUser(@RequestHeader(value = "Authorization", required = true) String userJwt)
            throws JWTValidationException, JWTCreationException, UserRetrievalException, MultipleUserAccountsException {
        User user = userConverter.getImpersonatingUser(userJwt.split(" ")[1]);
        String jwt = authenticationManager.unimpersonateUser(user);
        String jwtJSON = "{\"token\": \"" + jwt + "\"}";
        return jwtJSON;
    }
}
