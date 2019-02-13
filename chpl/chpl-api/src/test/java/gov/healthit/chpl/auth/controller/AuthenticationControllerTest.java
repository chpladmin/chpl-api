package gov.healthit.chpl.auth.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UpdateExpiredPasswordRequest;
import gov.healthit.chpl.auth.user.UpdatePasswordResponse;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.web.controller.AuthenticationController;

/**
 * Tests for authentication controller.
 * @author alarned
 *
 */
public class AuthenticationControllerTest {
    @Spy
    private UserManager userManager;

    @Mock
    private Authenticator authenticator;

    @Mock
    private JWTUserConverter userConverter;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AuthenticationController myController;

    /**
     * Set up Mockito annotations.
     * @throws Exception if issue with mocks
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * OCD-2531: require users to change passwords.
     * @throws UserRetrievalException if user cannot be found
     * @throws JWTCreationException if JWT cannot be created
     * @throws JWTValidationException  if JWT cannot be validated
     */
    @Transactional
    @Test
    public void userCanUpdatePasswordWhenCredentialsExpired()
            throws UserRetrievalException, JWTCreationException, JWTValidationException {
        UpdateExpiredPasswordRequest req = new UpdateExpiredPasswordRequest();
        Strength strength = new Strength();
        req.setNewPassword("newPassword");
        req.setOldPassword("oldPassword");
        req.setUserName("username");
        strength.setScore(UserManager.MIN_PASSWORD_STRENGTH);

        doNothing().when(userManager).updateUserPassword(anyString(), anyString());
        when(userManager.getPasswordStrength(any(UserDTO.class), anyString())).thenReturn(strength);
        when(userManager.getEncodedPassword(any(UserDTO.class))).thenReturn("encodedPassword");
        when(authenticator.getUser(any(LoginCredentials.class))).thenReturn(getUserDtoTest());
        when(authenticator.getJWT(any(UserDTO.class))).thenReturn("a string");
        when(userConverter.getAuthenticatedUser(anyString())).thenReturn(getUserTest());
        when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UpdatePasswordResponse resp = myController.changeExpiredPassword(req);
        assertTrue(resp.isPasswordUpdated());
    }

    private User getUserTest() {
        User user = new JWTAuthenticatedUser();
        return user;
    }

    private UserDTO getUserDtoTest() {
        UserDTO user = new UserDTO();
        return user;
    }
}
