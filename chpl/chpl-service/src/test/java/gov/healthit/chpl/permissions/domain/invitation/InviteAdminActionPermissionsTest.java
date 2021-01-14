package gov.healthit.chpl.permissions.domain.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.invitation.InviteAdminActionPermissions;

public class InviteAdminActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private InviteAdminActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertTrue(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));

    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Only ROLE_ADMIN has access
        assertFalse(permissions.hasAccess());

        // This should always be false
        assertFalse(permissions.hasAccess(new Object()));
    }

}
