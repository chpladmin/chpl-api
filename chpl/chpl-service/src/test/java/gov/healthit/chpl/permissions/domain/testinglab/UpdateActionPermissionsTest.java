package gov.healthit.chpl.permissions.domain.testinglab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.testinglab.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAtlsForCurrentUser()).thenReturn(
                Stream.of(TestingLab.builder().id(2L).build()).toList());
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLab atl = new TestingLab();
        atl.setId(1L);
        assertTrue(permissions.hasAccess(atl));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLab atl = new TestingLab();
        atl.setId(1L);
        assertTrue(permissions.hasAccess(atl));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLab atl = new TestingLab();
        atl.setId(1L);
        assertFalse(permissions.hasAccess(atl));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLab atl = new TestingLab();
        atl.setId(1L);
        assertFalse(permissions.hasAccess(atl));

        atl = new TestingLab();
        atl.setId(2L);
        assertFalse(permissions.hasAccess(atl));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This is not used
        assertFalse(permissions.hasAccess());

        TestingLab atl = new TestingLab();
        atl.setId(1L);
        assertFalse(permissions.hasAccess(atl));
    }

}
