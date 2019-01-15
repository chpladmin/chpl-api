package gov.healthit.chpl.app.permissions.domain.pendingsurveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.GetAllActionPermissions;

public class GetAllActionPermissionsTest extends ActionPermissionsBaseTest {

    @Spy
    private CertificationBodyManager acbManager;

    @InjectMocks
    private GetAllActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(acbManager.getAllForUser())
        .thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        assertFalse(permissions.hasAccess());

        //This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        assertTrue(permissions.hasAccess());

        //This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        assertFalse(permissions.hasAccess());

        //This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        assertFalse(permissions.hasAccess());

        //This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        assertFalse(permissions.hasAccess());

        //This should always return false
        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

}
