package gov.healthit.chpl.permissions.domain.developer;

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
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.developer.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperDAO developerDAO;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new DeveloperDTO()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new DeveloperDTO()));
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

        assertFalse(permissions.hasAccess());

        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(1L);
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setDeveloperId(1L);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setStatusName("Active");
        statusEvent.setStatus(status);
        dto.getStatusEvents().add(statusEvent);

        Mockito.when(developerDAO.getById(ArgumentMatchers.anyLong())).thenReturn(dto);
        // If the current status is Active
        assertTrue(permissions.hasAccess(dto));

        // If the current status is Non-Active
        status.setStatusName("Suspended by ONC");
        dto.getStatusEvents().clear();
        statusEvent.setStatus(status);
        dto.getStatusEvents().add(statusEvent);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
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
