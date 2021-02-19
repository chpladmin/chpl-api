package gov.healthit.chpl.permissions.domain.changerequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ChangeRequestDAO changeRequestDAO;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() throws EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllDevelopersForCurrentUser()).thenReturn(getAllDeveloperForUser(2L, 4L));

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().developerId(2L).build())
                        .build());
    }

    @Override
    @Test
    @Ignore
    public void hasAccess_Developer() throws Exception {
        setupForDeveloperUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().developerId(2L).build())
                        .build());

        assertTrue(permissions.hasAccess(ChangeRequest.builder()
                .developer(Developer.builder().developerId(2L).build())
                .build()));

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().developerId(3l).build())
                        .build());

        assertFalse(permissions.hasAccess(ChangeRequest.builder()
                .developer(Developer.builder().developerId(3l).build())
                .build()));

    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Mockito.when(developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(getDeveloperAcbs());
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1L));
        assertTrue(permissions.hasAccess(ChangeRequest.builder().id(1L).build()));

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(5l));
        assertFalse(permissions.hasAccess(ChangeRequest.builder().id(1L).build()));
    }

    @Override
    @Test
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test

    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));

    }

    private List<CertificationBody> getDeveloperAcbs() {
        return new ArrayList<CertificationBody>(Arrays.asList(
                CertificationBody.builder().id(1L).build(),
                CertificationBody.builder().id(2L).build()));
    }

}
