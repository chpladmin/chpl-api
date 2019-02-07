package gov.healthit.chpl.app.permissions.domain.certificationbody;

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
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.domains.certificationbody.GetByIdActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class GetByIdActionPermissionsTest extends ActionPermissionsBaseTest {
    @Spy
    private UserPermissionsManager userPermissionsManager;

    @InjectMocks
    private GetByIdActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(userPermissionsManager.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

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
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

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
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

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
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

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
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

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
        // Not Used
        assertFalse(permissions.hasAccess());

        // Role atl does not have access
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));
    }
}
