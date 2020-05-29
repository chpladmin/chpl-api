package gov.healthit.chpl.auth.authentication;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface Authenticator {
    String authenticate(LoginCredentials credentials) throws JWTCreationException, UserRetrievalException;

    UserDTO getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException;

    String getJWT(UserDTO user) throws JWTCreationException;

    String getJWT(LoginCredentials credentials) throws JWTCreationException;

    String refreshJWT() throws JWTCreationException, UserRetrievalException;

    String impersonateUser(String username) throws UserRetrievalException, JWTCreationException, UserManagementException;

    String unimpersonateUser(User user) throws JWTCreationException, UserRetrievalException;

}
