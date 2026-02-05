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
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.testinglab.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new UpdateActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
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
