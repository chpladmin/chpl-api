package gov.healthit.chpl.auth.authentication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.ChplAccountStatusChecker;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UserAuthenticatorTest {
    private static final Long RESEND_CONFIRMATION_EMAIL_WINDOW_IN_DAYS = 180L;
    private JWTAuthor jwtAuthor;
    private UserManager userManager;
    private UserDAO userDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ChplAccountStatusChecker userDetailsChecker;
    private ErrorMessageUtil msgUtil;
    private InvitationManager invitationManager;

    @Before
    public void setup() throws UserRetrievalException, MultipleUserAccountsException, EmailNotSentException {
        jwtAuthor = Mockito.mock(JWTAuthor.class);
        userManager = Mockito.mock(UserManager.class);
        userDAO = Mockito.mock(UserDAO.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        userDetailsChecker = Mockito.mock(ChplAccountStatusChecker.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        invitationManager = Mockito.mock(InvitationManager.class);

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
            throws UserRetrievalException, MultipleUserAccountsException, AccountStatusException, ChplAccountEmailNotConfirmedException {
        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil, invitationManager, RESEND_CONFIRMATION_EMAIL_WINDOW_IN_DAYS);
        UserDTO user = authenticator.getUser(creds);

        assertNotNull(user);
    }

    @Test(expected = ChplAccountStatusException.class)
    public void getUser_UnknownUserName_ThrowsChplAccountStatusException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

        Mockito.when(userDAO.getByNameOrEmail(ArgumentMatchers.anyString()))
                .thenReturn(null);

        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(null, null, userDAO, null, null, msgUtil, invitationManager, RESEND_CONFIRMATION_EMAIL_WINDOW_IN_DAYS);
        authenticator.getUser(creds);

        fail();

    }

    @Test(expected = ChplAccountEmailNotConfirmedException.class)
    public void getUser_NoUserSignature_ThrowsChplAccountStatusException()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

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
                        .signatureDate(null)
                        .build());

        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil, invitationManager, RESEND_CONFIRMATION_EMAIL_WINDOW_IN_DAYS);
        authenticator.getUser(creds);

        fail();
    }

    @Test
    public void getUser_PasswordNotValid_ReturnsNullUser()
            throws BadCredentialsException, AccountStatusException, UserRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

        Mockito.when(bCryptPasswordEncoder.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(false);

        LoginCredentials creds = new LoginCredentials("username", "password");
        AuthenticationManager authenticator = new AuthenticationManager(jwtAuthor, userManager, userDAO, bCryptPasswordEncoder,
                userDetailsChecker, msgUtil, invitationManager, RESEND_CONFIRMATION_EMAIL_WINDOW_IN_DAYS);
        UserDTO authenticatedUser  = authenticator.getUser(creds);
        assertNull(authenticatedUser);
    }

}
