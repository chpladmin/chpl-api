package gov.healthit.chpl.permissions.domain.userpermissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAtlActionPermissions;

public class AddAtlActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private AddAtlActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAtlsForCurrentUser()).thenReturn(getAllAtlForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));

        dto = new TestingLabDTO();
        dto.setId(2L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

}
