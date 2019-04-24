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
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAtlPermissionsForUserActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class DeleteAllAtlPermissionsForUserActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private UserTestingLabMapDAO userTestingLabMapDAO;

    @InjectMocks
    private DeleteAllAtlPermissionsForUserActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAtlsForCurrentUser()).thenReturn(getAllAtlForUser(2l, 4l));
        Mockito.when(userTestingLabMapDAO.getByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(getUserCertificationBodyMapDTOs());
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(1l));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(1l));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1l));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(1l));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto = new TestingLabDTO();
        dto.setId(2L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Not Used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto = new TestingLabDTO();
        dto.setId(2L);
        assertFalse(permissions.hasAccess(dto));
    }

    private List<UserTestingLabMapDTO> getUserCertificationBodyMapDTOs() {
        List<UserTestingLabMapDTO> dtos = new ArrayList<UserTestingLabMapDTO>();

        UserDTO user = new UserDTO();
        user.setId(1l);

        UserTestingLabMapDTO dto1 = new UserTestingLabMapDTO();
        TestingLabDTO atl1 = new TestingLabDTO();
        atl1.setId(2l);
        dto1.setTestingLab(atl1);;
        dto1.setUser(user);

        UserTestingLabMapDTO dto2 = new UserTestingLabMapDTO();
        TestingLabDTO atl2 = new TestingLabDTO();
        atl2.setId(3l);
        dto2.setTestingLab(atl1);;
        dto2.setUser(user);

        dtos.add(dto1);
        dtos.add(dto2);

        return dtos;
    }
}
