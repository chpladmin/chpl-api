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
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.RejectActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class RejectActionPermissionsTest extends ActionPermissionsBaseTest {

    @Spy
    private CertificationBodyManager acbManager;

    @Spy
    private SurveillanceDAO survDAO;;

    @InjectMocks
    private RejectActionPermissions permissions;

    @Before
    public void setup() throws EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(acbManager.getAllForUser())
        .thenReturn(getAllAcbForUser(2l, 4l));

        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;
        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        //This should always return false
        assertFalse(permissions.hasAccess());


        Long id = 1l;

        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 3l));
        assertFalse(permissions.hasAccess(id));

        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 4l));
        assertTrue(permissions.hasAccess(id));

    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAtlUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;
        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getCmsUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;
        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;
        assertFalse(permissions.hasAccess(id));

    }

    private PendingSurveillanceEntity getPendingSurveillanceEntity(Long id, Long certifiedProductId, Long acbId) {
        PendingSurveillanceEntity entity = new PendingSurveillanceEntity();
        entity.setId(id);
        entity.setCertifiedProduct(new CertifiedProductEntity());
        entity.getCertifiedProduct().setId(certifiedProductId);
        entity.getCertifiedProduct().setCertificationBodyId(acbId);
        return entity;
    }

}
