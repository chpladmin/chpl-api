package gov.healthit.chpl.app.permissions.domain.testinglab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.testinglab.AllUsersOnAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class AllUsersOnAtlActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;;

    @InjectMocks
    private AllUsersOnAtlActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAtlsForCurrentUser()).thenReturn(getAllAtlForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));

        dto = new TestingLabDTO();
        dto.setId(2L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));

        dto = new TestingLabDTO();
        dto.setId(2L);
        assertTrue(permissions.hasAccess(dto));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        assertFalse(permissions.hasAccess(dto));
    }

}
