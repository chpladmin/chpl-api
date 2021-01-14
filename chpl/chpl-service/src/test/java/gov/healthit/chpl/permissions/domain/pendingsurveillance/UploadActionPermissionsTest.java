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
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.UploadActionPermissions;

public class UploadActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO cpDAO;

    @InjectMocks
    private UploadActionPermissions permissions;

    @Before
    public void setup() throws EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1l, 2l));
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

        Surveillance surv = new Surveillance();
        assertTrue(permissions.hasAccess(surv));
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

        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1l);

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1l, 2l));
        assertTrue(permissions.hasAccess(surv));

        Mockito.when(cpDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getCertifiedProduct(1l, 3l));
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        assertFalse(permissions.hasAccess(surv));
    }

}
