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
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.ConfirmActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class ConfirmActionPermissionsTest extends ActionPermissionsBaseTest {

    @Spy
    private CertificationBodyManager acbManager;

    @Spy
    private CertifiedProductDAO cpDAO;

    @InjectMocks
    private ConfirmActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(acbManager.getAllForUser())
        .thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin()  throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Acb()  throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        //Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
        .thenReturn(getCertifiedProduct(1l, 2l));

        //With the above mock, the user should have access
        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);
        assertTrue(permissions.hasAccess(surv));


        //Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong()))
        .thenReturn(getCertifiedProduct(1l, 3l));

        //With the above mock, the user should NOT have access
        surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);
        assertFalse(permissions.hasAccess(surv));
    }


    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }


    @Override
    @Test
    public void hasAccess_Cms() throws Exception{
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }


    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        //This should always return false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }
}
