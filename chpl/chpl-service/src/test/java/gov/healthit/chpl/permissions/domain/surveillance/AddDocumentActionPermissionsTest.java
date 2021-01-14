package gov.healthit.chpl.permissions.domain.surveillance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.surveillance.AddDocumentActionPermissions;

public class AddDocumentActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private SurveillanceDAO survDAO;

    @InjectMocks
    private AddDocumentActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is admin it has access to all - param value does not matter.
        Long id = 1L;
        assertTrue(permissions.hasAccess(id));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Since it is onc it has access to all - param value does not matter.
        Long id = 1L;
        assertTrue(permissions.hasAccess(id));
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
    @Ignore
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        Mockito.when(survDAO.getSurveillanceByNonconformityId(ArgumentMatchers.anyLong())).thenReturn(getSurveillanceEntity(2l));
        assertTrue(permissions.hasAccess(1l));

        Mockito.when(survDAO.getSurveillanceByNonconformityId(ArgumentMatchers.anyLong())).thenReturn(getSurveillanceEntity(1l));
        assertFalse(permissions.hasAccess(1l));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Atl has no access - the param shouldn't even matter
        Long id = 1L;
        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Cms has no access - the param shouldn't even matter
        // Atl has no access - the param shouldn't even matter
        Long id = 1L;
        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Anon has no access - the param shouldn't even matter
        // Atl has no access - the param shouldn't even matter
        Long id = 1L;
        assertFalse(permissions.hasAccess(id));
    }

    private SurveillanceEntity getSurveillanceEntity(Long acbId) {
        SurveillanceEntity entity = new SurveillanceEntity();
        CertifiedProductEntity cp = new CertifiedProductEntity();
        cp.setCertificationBodyId(acbId);
        entity.setCertifiedProduct(cp);

        SurveillanceRequirementEntity req = new SurveillanceRequirementEntity();
        SurveillanceNonconformityEntity nc = new SurveillanceNonconformityEntity();
        nc.setId(1l);
        req.getNonconformities().add(nc);
        entity.getSurveilledRequirements().add(req);

        return entity;
    }
}
