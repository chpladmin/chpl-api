package gov.healthit.chpl.app.permissions.domain.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.domains.invitation.InviteRoleNoAccessActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class InviteRoleNoAccessActionPermissionsTest extends ActionPermissionsBaseTest {
    @Spy
    private UserPermissionsManager userPermissionsManager;

    @InjectMocks
    private InviteRoleNoAccessActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(userPermissionsManager.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertTrue(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));

    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertTrue(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertTrue(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        // Only ROLE_ADMIN, ROLE_ONC, ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }
}
