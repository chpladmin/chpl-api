package gov.healthit.chpl.permissions.domain.certificationbody;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.certificationbody.GetByIdActionPermissions;

public class GetByIdActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;;

    @InjectMocks
    private GetByIdActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role admin has permissions to all
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role onc has permissions to all
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role acb has access based on permissions
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));

        dto.setId(2l);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role atl does not have access
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role cms does not have access
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        // Role atl does not have access
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));
    }
}
