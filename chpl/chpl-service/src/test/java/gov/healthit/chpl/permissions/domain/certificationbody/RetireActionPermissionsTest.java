package gov.healthit.chpl.permissions.domain.certificationbody;

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
import gov.healthit.chpl.permissions.domains.certificationbody.RetireActionPermissions;

public class RetireActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;;

    @InjectMocks
    private RetireActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertTrue(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertTrue(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Only ROLE_ADMIN and ROLE_ONC has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }
}
