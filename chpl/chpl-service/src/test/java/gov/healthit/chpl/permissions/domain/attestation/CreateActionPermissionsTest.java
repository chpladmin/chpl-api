package gov.healthit.chpl.permissions.domain.attestation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.attestation.CreateActionPermissions;

public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {
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
                        .developerId(1L)
                        .build())
                .build()));

        Mockito.when(developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(CertificationBody.builder().id(2L).build()));

        assertFalse(permissions.hasAccess(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .developerId(2L)
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


}
