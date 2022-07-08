package gov.healthit.chpl.permissions.domain.attestation;

import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Override
    public void hasAccess_Admin() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_Onc() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_OncStaff() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_Acb() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_Atl() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_Cms() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void hasAccess_Anon() throws Exception {
        // TODO Auto-generated method stub

    }
    /************
    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllDevelopersForCurrentUser()).thenReturn(
                Arrays.asList(Developer.builder().id(1L).build()));

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBodyDTO.builder().id(1L).build()));

        Mockito.when(developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(CertificationBody.builder().id(1L).build()));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        // Not used
        assertTrue(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());


        assertTrue(permissions.hasAccess(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .id(1L)
                        .build())
                .build()));

        Mockito.when(developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(CertificationBody.builder().id(2L).build()));

        assertFalse(permissions.hasAccess(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .id(2L)
                        .build())
                .build()));
    }

    @Override
    @Test
    public void hasAccess_Developer() throws Exception {
        setupForDeveloperUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        // Not used
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(new DeveloperAttestationSubmission()));
    }

    ************/

}
