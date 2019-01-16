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
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
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
    private SurveillanceDAO survDAO;

    @Spy
    private UserPermissionDAO userPermissionDAO;

    @InjectMocks
    private RejectActionPermissions permissions;

    @Before
    public void setup() throws EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(acbManager.getAllForUser())
        .thenReturn(getAllAcbForUser(2l, 4l));

        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l, ROLE_ACB_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("", "", ""));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAdminUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;

        //Check where authority matches
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l, ROLE_ONC_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ONC", "", ""));

        assertTrue(permissions.hasAccess(id));

        //Check where authority is not correct for role
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l, ROLE_ACB_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ACB", "", ""));

        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getOncUser());

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;

        //Check where authority matches the user's role
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l, ROLE_ONC_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ONC", "", ""));

        assertTrue(permissions.hasAccess(id));

        //Check where authority does not match the user's role
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 1l, ROLE_ACB_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ACB", "", ""));


        assertFalse(permissions.hasAccess(id));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        //This should always return false
        assertFalse(permissions.hasAccess());


        Long id = 1l;

        //The user does not have access to this acb
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 3l, ROLE_ACB_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ACB", "", ""));

        assertFalse(permissions.hasAccess(id));

        //Should work...
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 4l, ROLE_ACB_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ACB", "", ""));

        assertTrue(permissions.hasAccess(id));

        //This one belongs to the wrong authority....
        Mockito.when(survDAO.getPendingSurveillanceById(
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
        .thenReturn(getPendingSurveillanceEntity(1l, 1l, 4l, ROLE_ONC_ID));

        Mockito.when(userPermissionDAO.findById(ArgumentMatchers.anyLong()))
        .thenReturn(getUserPermissionDTO("ROLE_ONC", "", ""));

        assertFalse(permissions.hasAccess(id));
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
        SecurityContextHolder.getContext().setAuthentication(null);

        //This should always return false
        assertFalse(permissions.hasAccess());

        Long id = 1l;
        assertFalse(permissions.hasAccess(id));

    }

    private PendingSurveillanceEntity getPendingSurveillanceEntity(Long id, Long certifiedProductId, Long acbId, Long userPermissionId) {
        PendingSurveillanceEntity entity = new PendingSurveillanceEntity();
        entity.setId(id);
        entity.setCertifiedProduct(new CertifiedProductEntity());
        entity.getCertifiedProduct().setId(certifiedProductId);
        entity.getCertifiedProduct().setCertificationBodyId(acbId);
        entity.setUserPermissionId(userPermissionId);
        return entity;
    }

    private UserPermissionDTO getUserPermissionDTO(String authority, String name, String description) {
        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setAuthority(authority);
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }

}
