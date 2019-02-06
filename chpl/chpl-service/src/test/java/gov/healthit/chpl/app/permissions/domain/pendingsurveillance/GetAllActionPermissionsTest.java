package gov.healthit.chpl.app.permissions.domain.pendingsurveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.GetAllActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class GetAllActionPermissionsTest extends ActionPermissionsBaseTest {

    @Spy
    private UserPermissionsManager userPermissionsManager;

    @Spy
    private CertifiedProductDAO cpDAO;

    @InjectMocks
    private GetAllActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(userPermissionsManager.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        assertTrue(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setAuthority("ROLE_ONC");
        assertTrue(permissions.hasAccess(surv));

        surv = new Surveillance();
        surv.setAuthority("ROLE_ACB");
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        assertTrue(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setAuthority("ROLE_ONC");
        assertTrue(permissions.hasAccess(surv));

        surv = new Surveillance();
        surv.setAuthority("ROLE_ACB");
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        assertTrue(permissions.hasAccess());

        // Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1l, 2l));

        // With the above mock, the user should have access
        // The ACB is correct, but the authority is incorrect
        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);
        surv.setAuthority("ROLE_ACB");
        assertTrue(permissions.hasAccess(surv));

        // With the above mock, the user should have access.
        // The ACB is correct, and the authority is correct
        surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);
        surv.setAuthority("ROLE_ONC");
        assertFalse(permissions.hasAccess(surv));

        // Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1l, 3l));

        // With the above mock, the user should NOT have access
        // The ACB is incorrect, but the authority is correct
        surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);
        surv.setAuthority("ROLE_ACB");
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        assertFalse(permissions.hasAccess());

        // This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        assertFalse(permissions.hasAccess());

        // This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertFalse(permissions.hasAccess());

        // This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

}
