package gov.healthit.chpl.manager.auth;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserResetTokenDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UserManagerTest {
    private static final String MAX_FAILED_LOGIN_ATTEMPTS = "3";
    private static final String RESET_LINK_EXPIRATION = "1";
    private static final int FIVE_HOURS_AGO = -5;
    private static final int FOUR_FAILED_LOGIN_ATTEMPTS = 4;

    private UserDAO userDAO;
    private MutableAclService mutableAclService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ActivityManager activityManager;
    private ErrorMessageUtil errorMessageUtil;
    private Environment env;
    private UserResetTokenDAO userResetTokenDAO;

    @Before
    public void setup()
            throws UserCreationException, UserRetrievalException, JsonProcessingException, EntityCreationException,
            EntityRetrievalException {

        userDAO = Mockito.mock(UserDAO.class);
        mutableAclService = Mockito.mock(MutableAclService.class);
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
        Mockito.doNothing()
                .when(userDAO)
                .delete(ArgumentMatchers.anyLong());
        Mockito.doNothing()
                .when(userDAO)
                .updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
        Mockito.doNothing()
                .when(userDAO)
                .updateAccountLockedStatus(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
        Mockito.when(userDAO.findUserByNameAndEmail(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(getUserDTO(1L));

        Mockito.when(mutableAclService.readAclById(ArgumentMatchers.any(ObjectIdentity.class)))
                .thenReturn(getMutabeAcl());
        Mockito.when(mutableAclService.updateAcl(ArgumentMatchers.any(MutableAcl.class)))
                .thenReturn(null);
        Mockito.doNothing()
                .when(mutableAclService)
                .deleteAcl(ArgumentMatchers.any(ObjectIdentity.class), ArgumentMatchers.anyBoolean());

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
    public void create_GoodData_Success() throws UserCreationException {

        UserDTO user = getUserDTO(1L);
        UserManager userManager = new UserManager(null, userDAO, null, bCryptPasswordEncoder, mutableAclService, null, null);

        UserDTO newUser = userManager.create(user, "@ThisShouldBeAStrongPassword1!");

        assertNotNull(newUser);
    }

    @Test(expected = UserCreationException.class)
    public void create_WeakPassword_UserCreationException() throws UserCreationException {
        Mockito.when(userDAO.create(ArgumentMatchers.any(UserDTO.class), ArgumentMatchers.anyString()))
                .thenThrow(new UserCreationException());

        UserDTO user = getUserDTO(1L);

        UserManager userManager = new UserManager(null, userDAO, null, bCryptPasswordEncoder, mutableAclService, null, null);

        userManager.create(user, "@ThisShouldBeAStrongPassword1!");

        fail();
    }

    @Test
    public void update_GoodData_SuccessAndActivityWritten()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException {

        UserManager userManager = new UserManager(null, userDAO, null, null, null, null, activityManager);

        UserDTO user = getUserDTO(1L);
        UserDTO updatedUser = userManager.update(user);

        assertNotNull(updatedUser);

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test(expected = ValidationException.class)
    public void update_MissingFullName_ValidationExceptionThrown()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException {

        UserManager userManager = new UserManager(null, null, null, null, null, errorMessageUtil, null);

        UserDTO user = getUserDTO(1L);
        user.setFullName("");
        userManager.update(user);

        fail();
    }

    @Test(expected = ValidationException.class)
    public void update_MissingEmail_ValidationExceptionThrown()
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException {

        UserManager userManager = new UserManager(null, null, null, null, null, errorMessageUtil, null);

        UserDTO user = getUserDTO(1L);
        user.setFullName("");
        userManager.update(user);

        fail();
    }

    @Test(expected = ValidationException.class)
    public void update_MissingPhoneNumber_ValidationExceptionThrown()
            throws JsonProcessingException, UserRetrievalException, EntityCreationException, EntityRetrievalException,
            ValidationException {

        UserManager userManager = new UserManager(null, null, null, null, null, errorMessageUtil, null);

        UserDTO user = getUserDTO(1L);
        user.setPhoneNumber("");
        userManager.update(user);

        fail();
    }

    @Test()
    public void update_MissingEmailAndPhoneNumber_ValidationExceptionThrown()
            throws JsonProcessingException, UserRetrievalException, EntityCreationException, EntityRetrievalException {

        UserManager userManager = new UserManager(null, null, null, null, null, errorMessageUtil, null);

        UserDTO user = getUserDTO(1L);
        user.setPhoneNumber("");
        user.setEmail("");
        try {
            userManager.update(user);
        } catch (ValidationException e) {
            assertEquals(2, e.getErrorMessages().size());
            return;
        }
        fail();
    }

    @Test()
    public void delete_GoodData_Success()
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {

        UserManager userManager = new UserManager(null, userDAO, null, null, mutableAclService, null, null);
        userManager.delete(getUserDTO(1L));

        Mockito.verify(mutableAclService).deleteAcl(ArgumentMatchers.any(ObjectIdentity.class), ArgumentMatchers.anyBoolean());
        Mockito.verify(userDAO).delete(1L);
    }

    @Test(expected = UserRetrievalException.class)
    public void delete_UserNotValid_UserRetrievalExceptionThrown()
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
        Mockito.doThrow(UserRetrievalException.class).when(userDAO).delete(ArgumentMatchers.anyLong());

        UserManager userManager = new UserManager(null, userDAO, null, null, mutableAclService, null, null);
        userManager.delete(getUserDTO(1L));

        fail();
    }

    @Test
    public void updateFailedLoginCount_FailedLoginCountLessThanMaxFailedAttempts_NoErrors()
            throws UserRetrievalException {

        UserManager userManager = new UserManager(env, userDAO, null, null, null, null, null);

        userManager.updateFailedLoginCount(getUserDTO(1L));

        Mockito.verify(userDAO).updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
    }

    @Test(expected = UserRetrievalException.class)
    public void updateFailedLoginCount_UserNotFound_UserRetrievalExceptionThrown() throws UserRetrievalException {

        Mockito.doThrow(UserRetrievalException.class).when(userDAO).updateFailedLoginCount(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());

        UserManager userManager = new UserManager(null, userDAO, null, null, null, null, null);

        userManager.updateFailedLoginCount(getUserDTO(1L));

        fail();
    }

    @Test
    public void updateFailedLoginCount_FailedLoginCountGreaterThanMaxFailedAttempts_AccountIsLocked()
            throws UserRetrievalException {

        UserManager userManager = new UserManager(env, userDAO, null, null, null, null, null);

        UserDTO user = getUserDTO(1L);
        user.setFailedLoginCount(FOUR_FAILED_LOGIN_ATTEMPTS);
        userManager.updateFailedLoginCount(user);

        Mockito.verify(userDAO).updateFailedLoginCount(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());
        Mockito.verify(userDAO).updateAccountLockedStatus(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
    }

    @Test
    public void createResetUserPasswordToken_UserIsValid_ValidUserResetTokenDTO() throws UserRetrievalException {
        UserManager userManager = new UserManager(null, userDAO, userResetTokenDAO, null, null, null, null);
        UserResetTokenDTO token = userManager.createResetUserPasswordToken("user_a", "abc@def.com");

        assertNotNull(token);
    }

    @Test(expected = UserRetrievalException.class)
    public void createResetUserPasswordToken_UserIsNotValid_UserRetrievalExceptionThrown() throws UserRetrievalException {
        Mockito.when(userDAO.findUserByNameAndEmail(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(null);

        UserManager userManager = new UserManager(null, userDAO, userResetTokenDAO, null, null, null, null);
        userManager.createResetUserPasswordToken("user_a", "abc@def.com");

        fail();
    }

    @Test
    public void authorizePasswordReset_ValidToken_ReturnTrue() {
        UserManager userManager = new UserManager(env, null, userResetTokenDAO, null, null, null, null);
        boolean auth = userManager.authorizePasswordReset("token");

        assertEquals(true, auth);
    }

    @Test
    public void authorizePasswordReset_TokenNotFound_ReturnFalse() {
        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
                .thenReturn(null);

        UserManager userManager = new UserManager(null, null, userResetTokenDAO, null, null, null, null);
        boolean auth = userManager.authorizePasswordReset("token");

        assertEquals(false, auth);
    }

    @Test
    public void authorizePasswordReset_InvalidToken_ReturnFalse() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.add(Calendar.HOUR, FIVE_HOURS_AGO);

        Mockito.when(userResetTokenDAO.findByAuthToken(ArgumentMatchers.anyString()))
                .thenReturn(UserResetTokenDTO.builder()
                        .creationDate(oldDate.getTime())
                        .build());

        UserManager userManager = new UserManager(env, null, userResetTokenDAO, null, null, null, null);
        boolean auth = userManager.authorizePasswordReset("token");

        assertEquals(false, auth);
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
                .subjectName("user_a")
                .title("Sr Eng")
                .build();
    }

    private MutableAcl getMutabeAcl() {
        return new MutableAcl() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isSidLoaded(List<Sid> sids) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isGranted(List<Permission> permission, List<Sid> sids, boolean administrativeMode)
                    throws NotFoundException, UnloadedSidException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isEntriesInheriting() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Acl getParentAcl() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Sid getOwner() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ObjectIdentity getObjectIdentity() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<AccessControlEntry> getEntries() {
                return new ArrayList<AccessControlEntry>();
            }

            @Override
            public void updateAce(int aceIndex, Permission permission) throws NotFoundException {
                // TODO Auto-generated method stub

            }

            @Override
            public void setParent(Acl newParent) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setOwner(Sid newOwner) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setEntriesInheriting(boolean entriesInheriting) {
                // TODO Auto-generated method stub

            }

            @Override
            public void insertAce(int atIndexLocation, Permission permission, Sid sid, boolean granting)
                    throws NotFoundException {
                // TODO Auto-generated method stub

            }

            @Override
            public Serializable getId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void deleteAce(int aceIndex) throws NotFoundException {
                // TODO Auto-generated method stub

            }
        };
    }

}
