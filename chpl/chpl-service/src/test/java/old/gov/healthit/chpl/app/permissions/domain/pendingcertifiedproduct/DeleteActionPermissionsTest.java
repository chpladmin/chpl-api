package old.gov.healthit.chpl.app.permissions.domain.pendingcertifiedproduct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.DeleteActionPermissions;
import old.gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
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
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // ROLE_ONC does not have access
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Setup Mock
        Mockito.when(pcpDao.findAcbIdById(ArgumentMatchers.anyLong())).thenReturn(2L);

        // This should always be false
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(-1L));
    }

    @Test
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
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(1L));
    }
}
