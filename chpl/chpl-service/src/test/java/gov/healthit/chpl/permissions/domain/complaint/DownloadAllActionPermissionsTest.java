package gov.healthit.chpl.permissions.domain.complaint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.complaint.DownloadAllActionPermissions;

public class DownloadAllActionPermissionsTest extends ActionPermissionsBaseTest {
    private AutoCloseable closeableMocks;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFacotry;

    @InjectMocks
    private DownloadAllActionPermissions permissions;

    @Before
    public void setup() {
        closeableMocks = MockitoAnnotations.openMocks(this);
        Mockito.when(resourcePermissionsFacotry.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @After
    public void teardown() {
        if (closeableMocks != null) {
            try {
                closeableMocks.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertTrue(permissions.hasAccess());

        Complaint complaint = new Complaint();
        assertFalse(permissions.hasAccess(complaint));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertTrue(permissions.hasAccess());

        Complaint complaint = new Complaint();
        assertFalse(permissions.hasAccess(complaint));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Complaint complaint = new Complaint();
        assertFalse(permissions.hasAccess(complaint));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Complaint complaint = new Complaint();
        assertFalse(permissions.hasAccess(complaint));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Complaint complaint = new Complaint();
        assertFalse(permissions.hasAccess(complaint));
    }

}
