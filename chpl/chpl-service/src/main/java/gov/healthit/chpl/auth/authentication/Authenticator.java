package gov.healthit.chpl.auth.authentication;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;


public interface Authenticator {

    public UserDTO getUser(LoginCredentials credentials) throws BadCredentialsException, AccountStatusException, UserRetrievalException;
    public String getJWT(UserDTO user) throws JWTCreationException;
    public String getJWT(LoginCredentials credentials) throws JWTCreationException;
    public String refreshJWT() throws JWTCreationException, UserRetrievalException;
    public String impersonateUser(String username) throws UserRetrievalException, JWTCreationException, UserManagementException;
    public String unimpersonateUser(User user) throws JWTCreationException, UserRetrievalException;

}