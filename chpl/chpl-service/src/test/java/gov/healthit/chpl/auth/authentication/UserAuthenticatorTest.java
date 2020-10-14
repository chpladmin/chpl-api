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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import gov.healthit.chpl.auth.ChplAccountStatusChecker;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UserAuthenticatorTest {
    private JWTAuthor jwtAuthor;
    private UserManager userManager;
    private UserDAO userDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ChplAccountStatusChecker userDetailsChecker;
    private ErrorMessageUtil msgUtil;

    @Before
    public void setup() throws UserRetrievalException, MultipleUserAccountsException {
        jwtAuthor = Mockito.mock(JWTAuthor.class);
        userManager = Mockito.mock(UserManager.class);
        userDAO = Mockito.mock(UserDAO.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        userDetailsChecker = Mockito.mock(ChplAccountStatusChecker.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

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

        Mockito.when(userDAO.getByNameOrEmail(ArgumentMatchers.anyString()))
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
    public void getUser_ValidLoginCredentials_ReturnValidUserDTO()
            throws UserRetrievalException, MultipleUserAccountsException {
        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil);
        UserDTO user = authenticator.getUser(creds);

        assertNotNull(user);
    }

    @Test(expected = ChplAccountStatusException.class)
    public void getUser_UnknownUserName_ThrowsChplAccountStatusException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException {

        Mockito.when(userDAO.getByNameOrEmail(ArgumentMatchers.anyString()))
                .thenReturn(null);

        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(null, null, userDAO, null, null, msgUtil);
        authenticator.getUser(creds);

        fail();

    }

    @Test(expected = ChplAccountStatusException.class)
    public void getUser_NoUserSignature_ThrowsChplAccountStatusException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException {

        Mockito.when(userDAO.getByNameOrEmail(ArgumentMatchers.anyString()))
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
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil);
        authenticator.getUser(creds);

        fail();
    }

    @Test(expected = ChplAccountStatusException.class)
    public void getUser_PasswordNotValid_ThrowsChplAccountStatusException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException {

        Mockito.when(bCryptPasswordEncoder.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(false);

        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil);
        authenticator.getUser(creds);

        fail();
    }

}
