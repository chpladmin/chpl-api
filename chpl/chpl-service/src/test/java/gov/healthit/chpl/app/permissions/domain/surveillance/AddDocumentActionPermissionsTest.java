package gov.healthit.chpl.app.permissions.domain.surveillance;

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
import gov.healthit.chpl.permissions.domains.surveillance.AddDocumentActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class AddDocumentActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions permissionChecker;

    @InjectMocks
    private AddDocumentActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(permissionChecker.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is onc it has access to all - param value does not matter.
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1l);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(2l);
        assertTrue(permissions.hasAccess(dto));

        dto.setId(1l);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(2l);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        // Atl has no access - the param shouldn't even matter
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(2l);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        // Atl has no access - the param shouldn't even matter
        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(2l);
        assertFalse(permissions.hasAccess(dto));
    }
}
