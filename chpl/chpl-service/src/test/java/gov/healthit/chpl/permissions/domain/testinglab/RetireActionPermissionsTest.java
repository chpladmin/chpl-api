package gov.healthit.chpl.permissions.domain.testinglab;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.testinglab.RetireActionPermissions;

public class RetireActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private RetireActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new RetireActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertTrue(permissions.hasAccess());

        // This is not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertTrue(permissions.hasAccess());

        // This is not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        assertFalse(permissions.hasAccess());

        // This is not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        // This is not used
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        // This is not used
        assertFalse(permissions.hasAccess(new Object()));
    }

}
