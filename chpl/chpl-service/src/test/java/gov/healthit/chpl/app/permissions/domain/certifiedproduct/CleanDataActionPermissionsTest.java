package gov.healthit.chpl.app.permissions.domain.certifiedproduct;

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
import gov.healthit.chpl.permissions.domains.certifiedproduct.CleanDataActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class CleanDataActionPermissionsTest extends ActionPermissionsBaseTest {
    @Spy
    private UserPermissionsManager userPermissionsManager;

    @InjectMocks
    private CleanDataActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(userPermissionsManager.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - parm value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // ROLE_ONC does not have access
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(1L));

        assertTrue(permissions.hasAccess(2L));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}
