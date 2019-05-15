package gov.healthit.chpl.app.permissions.domain.userpermissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAcbPermissionsForUserActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class DeleteAllAcbPermissionsForUserActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;

    @InjectMocks
    private DeleteAllAcbPermissionsForUserActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2l, 4l));
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(getUserCertificationBodyMapDTOs());
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess(3l));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(3l));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess(3L));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // ATL should have access
        assertFalse(permissions.hasAccess(3L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(3L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    private List<UserCertificationBodyMapDTO> getUserCertificationBodyMapDTOs() {
        List<UserCertificationBodyMapDTO> dtos = new ArrayList<UserCertificationBodyMapDTO>();

        UserDTO user = new UserDTO();
        user.setId(1l);

        UserCertificationBodyMapDTO dto1 = new UserCertificationBodyMapDTO();
        CertificationBodyDTO acb1 = new CertificationBodyDTO();
        acb1.setId(2l);
        dto1.setCertificationBody(acb1);;
        dto1.setUser(user);

        UserCertificationBodyMapDTO dto2 = new UserCertificationBodyMapDTO();
        CertificationBodyDTO acb2 = new CertificationBodyDTO();
        acb2.setId(3l);
        dto2.setCertificationBody(acb1);;
        dto2.setUser(user);

        dtos.add(dto1);
        dtos.add(dto2);

        return dtos;
    }
}
