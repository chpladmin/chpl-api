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

    public UserDTO getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException;
    public String getJWT(UserDTO user) throws JWTCreationException;
    public String getJWT(LoginCredentials credentials) throws JWTCreationException;
    public String refreshJWT() throws JWTCreationException, UserRetrievalException;
    public String impersonateUser(String username) throws UserRetrievalException, JWTCreationException, UserManagementException;
    public String unimpersonateUser(User user) throws JWTCreationException, UserRetrievalException;

}