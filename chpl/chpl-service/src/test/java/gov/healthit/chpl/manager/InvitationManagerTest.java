package gov.healthit.chpl.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.auth.InvitationDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.InvitationEmailer;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class InvitationManagerTest {

    private InvitationDAO invitationDao;
    private UserDAO userDao;
    private UserManager userManager;
    private UserPermissionsManager userPermissionsManager;
    private InvitationEmailer invitationEmailer;
    private ActivityManager activityManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    private UserPermissionDAO userPermissionDao;

    private InvitationManager invitationManager;

    @Before
    public void setup() {
        invitationDao = Mockito.mock(InvitationDAO.class);
        userDao = Mockito.mock(UserDAO.class);
        userManager = Mockito.mock(UserManager.class);
        userPermissionsManager = Mockito.mock(UserPermissionsManager.class);
        invitationEmailer = Mockito.mock(InvitationEmailer.class);
        activityManager = Mockito.mock(ActivityManager.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        userPermissionDao = Mockito.mock(UserPermissionDAO.class);
        Mockito.when(userPermissionDao.findAll()).thenReturn(new ArrayList<UserPermission>());

        invitationManager = new InvitationManager(userPermissionDao, invitationDao, userDao,
                userManager, userPermissionsManager, invitationEmailer, activityManager,
                resourcePermissions, msgUtil);
    }

    @Test
    public void getByCreatedUserId_UserIdExists_ReturnsNonNullUserInvitation() {
        Mockito.when(invitationDao.getByCreatedUserId(ArgumentMatchers.anyLong()))
                .thenReturn(UserInvitation.builder().build());

        UserInvitation userInvitation = invitationManager.getByCreatedUserId(1L);

        assertNotNull(userInvitation);
    }

    @Test
    public void resendConfirmEmailsAddressEmailToUser_UserExists_ReturnsTrue() throws UserRetrievalException {
        Mockito.doNothing().when(invitationEmailer).emailNewUser(
                ArgumentMatchers.isA(UserDTO.class),
                ArgumentMatchers.isA(UserInvitation.class));
        Mockito.when(invitationDao.getByCreatedUserId(ArgumentMatchers.anyLong()))
                .thenReturn(UserInvitation.builder().build());
        Mockito.when(userDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(UserDTO.builder().id(1L).build());


        Boolean emailSent = invitationManager.resendConfirmAddressEmailToUser(1L);

        assertEquals(true, emailSent);
    }

    @Test
    public void resendConfirmEmailsAddressEmailToUser_UserIsNull_ReturnsFalse() throws UserRetrievalException {
        Mockito.doNothing().when(invitationEmailer).emailNewUser(
                ArgumentMatchers.isA(UserDTO.class),
                ArgumentMatchers.isA(UserInvitation.class));
        Mockito.when(invitationDao.getByCreatedUserId(ArgumentMatchers.anyLong()))
                .thenReturn(UserInvitation.builder().build());
        Mockito.when(userDao.getById(ArgumentMatchers.anyLong())).thenReturn(null);


        Boolean emailSent = invitationManager.resendConfirmAddressEmailToUser(1L);

        assertEquals(false, emailSent);
    }

    @Test
    public void resendConfirmEmailsAddressEmailToUser_UserRetrievalException_ReturnsFalse() throws UserRetrievalException {
        Mockito.doNothing().when(invitationEmailer).emailNewUser(
                ArgumentMatchers.isA(UserDTO.class),
                ArgumentMatchers.isA(UserInvitation.class));
        Mockito.when(invitationDao.getByCreatedUserId(ArgumentMatchers.anyLong()))
                .thenReturn(UserInvitation.builder().build());
        Mockito.when(userDao.getById(ArgumentMatchers.anyLong())).thenThrow(new UserRetrievalException());

        Boolean emailSent = invitationManager.resendConfirmAddressEmailToUser(1L);

        assertEquals(false, emailSent);
    }

    @Test
    public void resendConfirmEmailsAddressEmailToUser_InvitationDoesNotExist_ReturnsFalse() throws UserRetrievalException {
        Mockito.doNothing().when(invitationEmailer).emailNewUser(
                ArgumentMatchers.isA(UserDTO.class),
                ArgumentMatchers.isA(UserInvitation.class));
        Mockito.when(invitationDao.getByCreatedUserId(ArgumentMatchers.anyLong()))
                .thenReturn(null);
        Mockito.when(userDao.getById(ArgumentMatchers.anyLong()))
                .thenReturn(UserDTO.builder().id(1L).build());


        Boolean emailSent = invitationManager.resendConfirmAddressEmailToUser(1L);

        assertEquals(false, emailSent);
    }

}
