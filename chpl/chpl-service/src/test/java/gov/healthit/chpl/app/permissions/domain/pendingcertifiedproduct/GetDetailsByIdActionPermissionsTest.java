package gov.healthit.chpl.app.permissions.domain.pendingcertifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class GetDetailsByIdActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions permissionChecker;

    @InjectMocks
    private GetDetailsByIdActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(permissionChecker.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertTrue(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertTrue(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        // Only ROLE_ADMIN and ROLE_ACB has access
        assertFalse(permissions.hasAccess());

        // Not used
        assertFalse(permissions.hasAccess(new Object()));
    }
}
