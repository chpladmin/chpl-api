package gov.healthit.chpl.permissions.domain.pendingsurveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.GetAllActionPermissions;

public class GetAllActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO cpDAO;

    @InjectMocks
    private GetAllActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertTrue(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertTrue(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertTrue(permissions.hasAccess());

        // Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1L, 2L));

        // With the above mock, the user should have access
        // The ACB is correct, but the authority is incorrect
        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1L);
        assertTrue(permissions.hasAccess(surv));

        // Setup Mock
        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1L, 3L));

        // With the above mock, the user should NOT have access
        // The ACB is incorrect, but the authority is correct
        surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1L);
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

}
