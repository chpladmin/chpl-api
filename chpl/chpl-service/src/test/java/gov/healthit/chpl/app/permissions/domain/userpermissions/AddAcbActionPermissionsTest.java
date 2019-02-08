package gov.healthit.chpl.app.permissions.domain.userpermissions;

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
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class AddAcbActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions permissionChecker;

    @InjectMocks
    private AddAcbActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(permissionChecker.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));

        dto.setId(4L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        // This is not used
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

}
