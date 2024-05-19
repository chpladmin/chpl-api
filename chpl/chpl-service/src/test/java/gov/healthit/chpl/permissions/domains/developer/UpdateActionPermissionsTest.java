package gov.healthit.chpl.permissions.domains.developer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.permissions.ChplResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ChplResourcePermissions resourcePermissions;

    @Mock
    private DeveloperDAO developerDAO;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFacotry;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissionsFacotry.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        Mockito.when(permissions.getResourcePermissions()).thenReturn(resourcePermissions);
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new Developer()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new Developer()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Developer dev = new Developer();
        dev.setId(1L);
        //no status changes means developer is "normal" and should be able to be edited
        Mockito.when(resourcePermissions.isDeveloperNotBannedOrSuspended(ArgumentMatchers.eq(1L))).thenReturn(true);
        assertTrue(permissions.hasAccess(dev));

        // If the current status is a banned/suspended value dev should not be able to be edited
        Mockito.when(resourcePermissions.isDeveloperNotBannedOrSuspended(ArgumentMatchers.eq(1L))).thenReturn(false);
        assertFalse(permissions.hasAccess(dev));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }
}
