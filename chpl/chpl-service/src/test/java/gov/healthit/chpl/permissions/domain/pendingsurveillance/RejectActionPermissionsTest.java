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

import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.dao.surveillance.PendingSurveillanceDAO;
import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.RejectActionPermissions;

public class RejectActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private PendingSurveillanceDAO pendingSurveillanceDAO;

    @Mock
    private UserPermissionDAO userPermissionDAO;

    @InjectMocks
    private RejectActionPermissions permissions;

    @Before
    public void setup() throws EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
        Mockito.when(pendingSurveillanceDAO.getPendingSurveillanceById(ArgumentMatchers.anyLong()))
                .thenReturn(getPendingSurveillanceEntity(1L, 1L, 1L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1L;

        Mockito.when(pendingSurveillanceDAO.getPendingSurveillanceById(ArgumentMatchers.anyLong()))
                .thenReturn(getPendingSurveillanceEntity(1L, 1L, 1L));
        assertTrue(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1L;

        // The user does not have access to this acb
        Mockito.when(pendingSurveillanceDAO.getPendingSurveillanceById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
                .thenReturn(getPendingSurveillanceEntity(1L, 1L, 3L));
        assertFalse(permissions.hasAccess(id));

        // Should work...
        Mockito.when(pendingSurveillanceDAO.getPendingSurveillanceById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
                .thenReturn(getPendingSurveillanceEntity(1L, 1L, 4L));
        assertTrue(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always return false
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));

    }

    private PendingSurveillanceEntity getPendingSurveillanceEntity(Long id, Long certifiedProductId, Long acbId) {
        PendingSurveillanceEntity entity = new PendingSurveillanceEntity();
        entity.setId(id);
        entity.setCertifiedProduct(new CertifiedProductSummaryEntity());
        entity.getCertifiedProduct().setId(certifiedProductId);
        entity.getCertifiedProduct().setCertificationBodyId(acbId);
        return entity;
    }
}
