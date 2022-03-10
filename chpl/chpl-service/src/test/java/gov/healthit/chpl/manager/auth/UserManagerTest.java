package gov.healthit.chpl.manager.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserResetTokenDAO;
import gov.healthit.chpl.domain.auth.ResetPasswordRequest;
import gov.healthit.chpl.domain.auth.UpdatePasswordResponse;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserAccountExistsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.UserMapper;

public class UserManagerTest {
    private static final String MAX_FAILED_LOGIN_ATTEMPTS = "3";
    private static final String RESET_LINK_EXPIRATION = "1";
    private static final int FIVE_HOURS_AGO = -5;
    private static final int FOUR_FAILED_LOGIN_ATTEMPTS = 4;

    private UserDAO userDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ActivityManager activityManager;
    private ErrorMessageUtil errorMessageUtil;
    private Environment env;
    private UserResetTokenDAO userResetTokenDAO;

    @Before
    public void setup()
            throws UserCreationException, UserRetrievalException, JsonProcessingException, EntityCreationException,
            EntityRetrievalException, MultipleUserAccountsException {

        userDAO = Mockito.mock(UserDAO.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        activityManager = Mockito.mock(ActivityManager.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        env = Mockito.mock(Environment.class);
        userResetTokenDAO = Mockito.mock(UserResetTokenDAO.class);

        Mockito.when(userDAO.create(ArgumentMatchers.any(UserDTO.class), ArgumentMatchers.anyString()))
                .thenAnswer(i -> i.getArgument(0));
        Mockito.when(userDAO.update(ArgumentMatchers.any(UserDTO.class)))
                .thenAnswer(i -> i.getArgument(0));
        Mockito.when(userDAO.getById(ArgumentMatchers.anyLong()))
                .thenAnswer(i -> getUserDTO(i.getArgument(0)));
        Mockito.when(userDAO.getByNameOrEmail(ArgumentMatchers.anyString()))
                .thenAnswer(i -> getUserDTO(1L));
        Mockito.doNothing()
                .when(userDAO)
                .delete(ArgumentMatchers.anyLong());
        Mockito.doNothing()
                .when(userDAO)
                .updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
        Mockito.doNothing()
                .when(userDAO)
                .updateAccountLockedStatus(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
        Mockito.when(userDAO.findUserByEmail(ArgumentMatchers.anyString()))
                .thenReturn(getUserDTO(1L));

        Mockito.when(bCryptPasswordEncoder.encode(ArgumentMatchers.anyString()))
                .thenAnswer(i -> i.getArgument(0));

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString()))
                .thenAnswer(i -> i.getArgument(0));

        Mockito.when(env.getProperty("authMaximumLoginAttempts"))
                .thenReturn(MAX_FAILED_LOGIN_ATTEMPTS);
        Mockito.when(env.getProperty("resetLinkExpirationTimeInHours"))
                .thenReturn(RESET_LINK_EXPIRATION);

        Mockito.doNothing()
                .when(activityManager)
                .addActivity(ArgumentMatchers.any(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(), ArgumentMatchers.any());

        Mockito.doNothing()
                .when(userResetTokenDAO)
                .deletePreviousUserTokens(ArgumentMatchers.anyLong());
        Mockito.when(userResetTokenDAO.create(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
                .thenReturn(UserResetTokenDTO.builder().build());
        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
                .thenReturn(UserResetTokenDTO.builder()
                        .creationDate(new Date())
                        .build());
    }

    @Test
    public void create_GoodData_Success() throws UserCreationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {

        UserDTO user = getUserDTO(1L);
        UserManager userManager = new UserManager(null, userDAO, null, bCryptPasswordEncoder,  null, activityManager, null, Mockito.mock(UserMapper.class));

        UserDTO newUser = userManager.create(user, "@ThisShouldBeAStrongPassword1!");

        assertNotNull(newUser);
    }

    @Test(expected = UserCreationException.class)
    public void create_WeakPassword_UserCreationException() throws UserCreationException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
        Mockito.when(userDAO.create(ArgumentMatchers.any(UserDTO.class), ArgumentMatchers.anyString()))
                .thenThrow(new UserCreationException());

        UserDTO user = getUserDTO(1L);

        UserManager userManager = new UserManager(null, userDAO, null, bCryptPasswordEncoder, null, null, null, Mockito.mock(UserMapper.class));

        userManager.create(user, "@ThisShouldBeAStrongPassword1!");

        fail();
    }

    @Test
    public void update_GoodData_SuccessAndActivityWritten()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException, MultipleUserAccountsException, UserAccountExistsException {

        UserManager userManager = new UserManager(null, userDAO, null, null, null, activityManager, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        UserDTO updatedUser = userManager.update(user);

        assertNotNull(updatedUser);

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test(expected = ValidationException.class)
    public void update_MissingFullName_ValidationExceptionThrown()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException, MultipleUserAccountsException, UserAccountExistsException {

        UserManager userManager = new UserManager(null, null, null, null, errorMessageUtil, null, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        user.setFullName("");
        userManager.update(user);

        fail();
    }

    @Test(expected = ValidationException.class)
    public void update_MissingEmail_ValidationExceptionThrown()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException, MultipleUserAccountsException, UserAccountExistsException {

        UserManager userManager = new UserManager(null, null, null, null, errorMessageUtil, null, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        user.setFullName("");
        userManager.update(user);

        fail();
    }

    @Test()
    public void update_MissingEmailAndPhoneNumber_ValidationExceptionThrown()
            throws JsonProcessingException, UserRetrievalException, EntityCreationException,
            EntityRetrievalException, MultipleUserAccountsException, UserAccountExistsException {

        UserManager userManager = new UserManager(null, null, null, null, errorMessageUtil, null, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        user.setPhoneNumber("");
        user.setEmail("");
        try {
            userManager.update(user);
        } catch (ValidationException e) {
            assertEquals(1, e.getErrorMessages().size());
            return;
        }
        fail();
    }

    @Test(expected = ValidationException.class)
    public void update_ChangedEmailAddress_ValidationExceptionThrown()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException, MultipleUserAccountsException, UserAccountExistsException {
        UserManager userManager = new UserManager(null, userDAO, null, null, errorMessageUtil, null, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        user.setEmail("anotheremail@test.com");
        userManager.update(user);

        fail();
    }

    @Test()
    public void delete_GoodData_Success()
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException{

        UserManager userManager = new UserManager(null, userDAO, null, null, null, activityManager, null, Mockito.mock(UserMapper.class));
        userManager.delete(getUserDTO(1L));

        Mockito.verify(userDAO).delete(1L);
    }

    @Test(expected = UserRetrievalException.class)
    public void delete_UserNotValid_UserRetrievalExceptionThrown()
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Mockito.doThrow(UserRetrievalException.class).when(userDAO).delete(ArgumentMatchers.anyLong());

        UserManager userManager = new UserManager(null, userDAO, null, null, null, null, null, Mockito.mock(UserMapper.class));
        userManager.delete(getUserDTO(1L));

        fail();
    }

    @Test
    public void updateFailedLoginCount_FailedLoginCountLessThanMaxFailedAttempts_NoErrors()
            throws UserRetrievalException, MultipleUserAccountsException, EmailNotSentException {

        UserManager userManager = new UserManager(env, userDAO, null, null, null, null, null, Mockito.mock(UserMapper.class));

        userManager.updateFailedLoginCount(getUserDTO(1L));

        Mockito.verify(userDAO).updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
    }

    @Test(expected = UserRetrievalException.class)
    public void updateFailedLoginCount_UserNotFound_UserRetrievalExceptionThrown()
            throws UserRetrievalException, MultipleUserAccountsException, EmailNotSentException {

        Mockito.doThrow(UserRetrievalException.class).when(userDAO).updateFailedLoginCount(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());

        UserManager userManager = new UserManager(null, userDAO, null, null, null, null, null, Mockito.mock(UserMapper.class));

        userManager.updateFailedLoginCount(getUserDTO(1L));

        fail();
    }

    @Test
    public void updateFailedLoginCount_FailedLoginCountGreaterThanMaxFailedAttempts_AccountIsLocked()
            throws UserRetrievalException, MultipleUserAccountsException, EmailNotSentException {

        UserManager userManager = new UserManager(env, userDAO, null, null, null, null, null, Mockito.mock(UserMapper.class));

        UserDTO user = getUserDTO(1L);
        user.setFailedLoginCount(FOUR_FAILED_LOGIN_ATTEMPTS);
        userManager.updateFailedLoginCount(user);

        Mockito.verify(userDAO).updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
        Mockito.verify(userDAO).updateAccountLockedStatus(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
    }

    @Test
    public void createResetUserPasswordToken_UserIsValid_ValidUserResetTokenDTO() throws UserRetrievalException {
        UserManager userManager = new UserManager(null, userDAO, userResetTokenDAO, null, null, null, null, Mockito.mock(UserMapper.class));
        UserResetTokenDTO token = userManager.createResetUserPasswordToken("abc@def.com");

        assertNotNull(token);
    }

    @Test(expected = UserRetrievalException.class)
    public void createResetUserPasswordToken_UserIsNotValid_UserRetrievalExceptionThrown() throws UserRetrievalException {
        Mockito.when(userDAO.findUserByEmail(ArgumentMatchers.anyString()))
                .thenReturn(null);

        UserManager userManager = new UserManager(null, userDAO, userResetTokenDAO, null, null, null, null, Mockito.mock(UserMapper.class));
        userManager.createResetUserPasswordToken("abc@def.com");

        fail();
    }

    @Test
    public void authorizePasswordReset_ValidToken_ReturnTrue() throws MultipleUserAccountsException, UserRetrievalException  {
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.from(ArgumentMatchers.any(UserEntity.class)))
            .thenReturn(UserDTO.builder()
                    .id(1L)
                    .email("test@test.com")
                    .fullName("test test")
                    .build());
        UserManager userManager = new UserManager(env, Mockito.mock(UserDAO.class),
                userResetTokenDAO, Mockito.mock(BCryptPasswordEncoder.class),
                null, null, null, userMapper);

        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
        .thenReturn(UserResetTokenDTO.builder()
                .deleted(false)
                .id(1L)
                .userId(1L)
                .user(new UserEntity())
                .userResetToken("token")
                .creationDate(new Date())
                .build());

        UpdatePasswordResponse response = userManager.authorizePasswordReset(ResetPasswordRequest.builder()
                .token("token")
                .newPassword("areallylongandG00dpassword")
                .build());

        assertNotNull(response);
        assertTrue(response.isPasswordUpdated());
    }

    @Test
    public void authorizePasswordReset_TokenNotFound_ReturnFalse() throws MultipleUserAccountsException, UserRetrievalException {
        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
                .thenReturn(null);

        UserManager userManager = new UserManager(null, null, userResetTokenDAO, null, null, null, null, Mockito.mock(UserMapper.class));
        UpdatePasswordResponse response = userManager.authorizePasswordReset(ResetPasswordRequest.builder()
                .token("token")
                .build());

        assertNotNull(response);
        assertFalse(response.isPasswordUpdated());
    }

    @Test
    public void authorizePasswordReset_InvalidToken_ReturnFalse() throws MultipleUserAccountsException, UserRetrievalException {
        Calendar oldDate = Calendar.getInstance();
        oldDate.add(Calendar.HOUR, FIVE_HOURS_AGO);

        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
                .thenReturn(UserResetTokenDTO.builder()
                        .creationDate(oldDate.getTime())
                        .build());
        UserManager userManager = new UserManager(env, null, userResetTokenDAO, null, null, null, null, Mockito.mock(UserMapper.class));
        UpdatePasswordResponse response = userManager.authorizePasswordReset(ResetPasswordRequest.builder()
                .token("token")
                .build());

        assertNotNull(response);
        assertFalse(response.isPasswordUpdated());
    }

    private UserDTO getUserDTO(Long id) {
        return UserDTO.builder()
                .accountEnabled(true)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .email("abc@def.com")
                .failedLoginCount(0)
                .friendlyName("Friendly Name")
                .fullName("Full Name")
                .id(id)
                .lastLoggedInDate(new Date())
                .phoneNumber("555-555-5555")
                .signatureDate(new Date())
                .title("Sr Eng")
                .build();
    }

}
