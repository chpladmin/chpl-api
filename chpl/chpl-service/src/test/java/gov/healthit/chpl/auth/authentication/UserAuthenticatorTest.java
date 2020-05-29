package gov.healthit.chpl.auth.authentication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.UserManager;

public class UserAuthenticatorTest {
    private JWTAuthor jwtAuthor;
    private UserManager userManager;
    private UserDAO userDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserDetailsChecker userDetailsChecker;

    @Before
    public void setup() throws UserRetrievalException {
        jwtAuthor = Mockito.mock(JWTAuthor.class);
        userManager = Mockito.mock(UserManager.class);
        userDAO = Mockito.mock(UserDAO.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        userDetailsChecker = Mockito.mock(UserDetailsChecker.class);

        Mockito.when(userDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(UserDTO.builder()
                        .id(1L)
                        .fullName("User Name")
                        .accountEnabled(true)
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .email("abc@def.com")
                        .failedLoginCount(0)
                        .passwordResetRequired(false)
                        .phoneNumber("555-555-5555")
                        .subjectName("userName")
                        .signatureDate(new Date())
                        .build());

        Mockito.when(bCryptPasswordEncoder.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(true);

        Mockito.doNothing()
                .when(userDetailsChecker).check(ArgumentMatchers.any(UserDTO.class));

        Mockito.doNothing()
                .when(userManager).updateLastLoggedInDate(ArgumentMatchers.any(UserDTO.class));
        Mockito.doNothing()
                .when(userManager).updateFailedLoginCount(ArgumentMatchers.any(UserDTO.class));
        Mockito.when(userManager.getEncodedPassword(ArgumentMatchers.any(UserDTO.class)))
                .thenReturn("encryptedPassword");

    }

    @Test()
    public void getUser_ValidLoginCredentials_ReturnValidUserDTO() throws UserRetrievalException {
        LoginCredentials creds = new LoginCredentials("username", "password");
        UserAuthenticator authenticator = new UserAuthenticator(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker);
        UserDTO user = authenticator.getUser(creds);

        assertNotNull(user);
    }

    @Test(expected = UserRetrievalException.class)
    public void getUser_UnknownUserName_ThrowsBadCredentialsException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException {

        Mockito.when(userDAO.getByName(ArgumentMatchers.anyString()))
                .thenThrow(UserRetrievalException.class);

        LoginCredentials creds = new LoginCredentials("username", "password");
        UserAuthenticator authenticator = new UserAuthenticator(null, null, userDAO, null, null);
        authenticator.getUser(creds);

        fail();

    }

    @Test(expected = BadCredentialsException.class)
    public void getUser_NoUserSignature_ThrowsBadCredentialsException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException {

        Mockito.when(userDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(UserDTO.builder()
                        .id(1L)
                        .fullName("User Name")
                        .accountEnabled(true)
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .email("abc@def.com")
                        .failedLoginCount(0)
                        .passwordResetRequired(false)
                        .phoneNumber("555-555-5555")
                        .subjectName("userName")
                        .signatureDate(null)
                        .build());

        LoginCredentials creds = new LoginCredentials("username", "password");
        UserAuthenticator authenticator = new UserAuthenticator(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker);
        authenticator.getUser(creds);

        fail();
    }

    @Test(expected = BadCredentialsException.class)
    public void getUser_PasswordNotValid_ThrowsBadCredentialsException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException {

        Mockito.when(bCryptPasswordEncoder.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(false);

        LoginCredentials creds = new LoginCredentials("username", "password");
        UserAuthenticator authenticator = new UserAuthenticator(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker);
        authenticator.getUser(creds);

        fail();
    }

}
