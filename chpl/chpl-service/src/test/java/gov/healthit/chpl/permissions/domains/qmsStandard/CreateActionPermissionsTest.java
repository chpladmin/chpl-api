package gov.healthit.chpl.permissions.domains.qmsStandard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {
    private AutoCloseable closeableMocks;

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        closeableMocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void teardown() {
        if (closeableMocks != null) {
            try {
                closeableMocks.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // This should always be false
        assertTrue(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // This should always be true
        assertTrue(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // This should always be false
        assertFalse(permissions.hasAccess());

        // Should always be false -- not used
        assertFalse(permissions.hasAccess(new Object()));
    }

}
