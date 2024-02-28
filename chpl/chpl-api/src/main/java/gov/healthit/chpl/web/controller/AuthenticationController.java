package gov.healthit.chpl.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.AuthenticationResponse;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.ResetPasswordRequest;
import gov.healthit.chpl.domain.auth.UpdatePasswordRequest;
import gov.healthit.chpl.domain.auth.UpdatePasswordResponse;
import gov.healthit.chpl.domain.auth.UserResetPasswordRequest;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.CognitoAuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.service.UserAccountUpdateEmailer;
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
    private UserAccountUpdateEmailer userAccountUpdateEmailer;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserManager userManager;
    private JWTUserConverterFacade userConverterFacade;
    private ErrorMessageUtil msgUtil;
    private CognitoAuthenticationManager cognitoAuthenticationManager;
    private FF4j ff4j;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
            UserAccountUpdateEmailer userAccountUpdateEmailer,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserManager userManager, JWTUserConverterFacade userConverterFacade, ErrorMessageUtil msgUtil,
            FF4j ff4j, CognitoAuthenticationManager cognitoAuthenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userAccountUpdateEmailer = userAccountUpdateEmailer;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userManager = userManager;
        this.userConverterFacade = userConverterFacade;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
        this.cognitoAuthenticationManager = cognitoAuthenticationManager;
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
    public AuthenticationResponse authenticateJSON(@RequestBody LoginCredentials credentials)
            throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

        String jwt = null;

        //If SSO is turned on try to auth using cognito
        if (ff4j.check(FeatureList.SSO)) {
            jwt = cognitoAuthenticationManager.authenticate(credentials);
        }

        //If SSO is turned off or Cognito auth fails, use the CHPL auth
        if (StringUtils.isEmpty(jwt)) {
            jwt = authenticationManager.authenticate(credentials);
        }

        if (jwt == null) {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    @Hidden
    @RequestMapping(value = "/keep-alive", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public AuthenticationResponse keepAlive(@RequestHeader (name = "Authorization") String token) throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException {
        //TODO Currently, keep-alive only works when logged in using CHPL id.  Cognito keep-alive will be implemented in the future
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwt = token.startsWith("Bearer") ? token.substring(7) : token;
        if (auth != null
                && auth instanceof JWTAuthenticatedUser
                && ((JWTAuthenticatedUser) auth).getAuthenticationSystem().equals(AuthenticationSystem.CHPL)) {

            jwt = authenticationManager.refreshJWT();
        }
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    @Operation(summary = "Change password.",
            description = "Change the logged in user's password as long as the old password "
                    + "passed in matches what is stored in the database.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/change-password", method = RequestMethod.POST,
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

    @Operation(summary = "Reset password.", description = "Reset the user's password.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/reset-password-request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public UpdatePasswordResponse resetPassword(@RequestBody ResetPasswordRequest request)
            throws UserRetrievalException, MultipleUserAccountsException {
        return userManager.authorizePasswordReset(request);
    }

    @Operation(summary = "Emails the user a token and link that can be used to reset their password..",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/email-reset-password", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void forgotPassword(@RequestBody UserResetPasswordRequest request)
            throws UserRetrievalException, EmailNotSentException {

        UserResetTokenDTO userResetTokenDTO = userManager.createResetUserPasswordToken(request.getEmail());
        userAccountUpdateEmailer.sendPasswordResetEmail(userResetTokenDTO.getUserResetToken(), request.getEmail());
    }

    @Operation(summary = "Impersonate another user.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/impersonate", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public AuthenticationResponse impersonateUser(@RequestHeader(value = "Authorization", required = true) String userJwt,
            @RequestParam(value = "id", required = true) Long id)
            throws UserRetrievalException, JWTCreationException, UserManagementException,
            JWTValidationException, MultipleUserAccountsException {

        String jwt = authenticationManager.impersonateUser(id);
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    @Operation(summary = "Stop impersonating another user.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/unimpersonate", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public AuthenticationResponse unimpersonateUser(@RequestHeader(value = "Authorization", required = true) String userJwt)
            throws JWTValidationException, JWTCreationException, UserRetrievalException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = userConverterFacade.getImpersonatingUser(userJwt.split(" ")[1]);

        String jwt = "";
        if (user != null) {
            jwt = authenticationManager.unimpersonateUser(user);
        } else {
            //This should be the scenario where someone tries to stop impersonating a Cognito user -- a scenario
            //that is not currently supported.  Returning the passed in token should have no effect.
            jwt = userJwt;
        }

        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }
}
