package gov.healthit.chpl.permissions.domain.pendingcertifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.DeleteActionPermissions;

public class DeleteActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Spy
    private PendingCertifiedProductDAO pcpDao;

    @InjectMocks
    private DeleteActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // ROLE_ONC does not have access
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Setup Mock
        Mockito.when(pcpDao.findAcbIdById(ArgumentMatchers.anyLong())).thenReturn(2L);

        // This should always be false
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(-1L));
    }

    @Test
    @Ignore
    public void hasNoAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Setup Mock
        Mockito.when(pcpDao.findAcbIdById(ArgumentMatchers.anyLong())).thenReturn(3L);

        // This should always be false
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(-1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}
