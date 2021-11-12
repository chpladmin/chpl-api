package gov.healthit.chpl.permissions.domain.surveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.CreateActionPermissions;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private CertifiedProductDAO certifiedProductDAO;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        try {
            CertifiedProductDTO listingNoAccess = new CertifiedProductDTO();
            listingNoAccess.setCertificationBodyId(1L);
            Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.eq(1L))).thenReturn(listingNoAccess);
            CertifiedProductDTO listingWithAccess = new CertifiedProductDTO();
            listingWithAccess.setCertificationBodyId(2L);
            Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.eq(2L))).thenReturn(listingWithAccess);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        assertTrue(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());
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
    @Ignore
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Surveillance surv = new Surveillance();
        surv.setCertifiedProduct(new CertifiedProduct());
        surv.getCertifiedProduct().setId(1L);

        Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getListing(1L));
        assertFalse(permissions.hasAccess(surv));

        Mockito.when(certifiedProductDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getListing(2L));
        assertTrue(permissions.hasAccess(surv));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        assertFalse(permissions.hasAccess(new Surveillance()));
    }

    private CertifiedProductDTO getListing(Long acbId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setCertificationBodyId(acbId);
        return dto;
    }
}
