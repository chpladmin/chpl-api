package old.gov.healthit.chpl.app.permissions.domain.changerequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;
import old.gov.healthit.chpl.app.permissions.domain.ActionPermissionsBaseTest;
import old.gov.healthit.chpl.changerequest.builders.CertificationBodyBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.DeveloperBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
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

        Mockito.when(resourcePermissions.getAllDevelopersForCurrentUser()).thenReturn(getAllDeveloperForUser(2l, 4l));

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder().withId(2l).build())
                        .build());
    }

    @Override
    @Test
    public void hasAccess_Developer() throws Exception {
        setupForDeveloperUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder().withId(2l).build())
                        .build());

        assertTrue(permissions.hasAccess(new ChangeRequestBuilder()
                .withId(1l)
                .withDeveloper(new DeveloperBuilder().withId(2l).build())
                .build()));

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDeveloper(new DeveloperBuilder().withId(3l).build())
                        .build());
        assertFalse(permissions.hasAccess(new ChangeRequestBuilder()
                .withId(1l)
                .withDeveloper(new DeveloperBuilder().withId(3l).build())
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
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Mockito.when(developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(ArgumentMatchers.anyLong()))
                .thenReturn(getDeveloperAcbs());
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1l));
        assertTrue(permissions.hasAccess(new ChangeRequestBuilder().withId(1l).build()));

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(5l));
        assertFalse(permissions.hasAccess(new ChangeRequestBuilder().withId(1l).build()));
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
                new CertificationBodyBuilder().withId(1l).build(),
                new CertificationBodyBuilder().withId(2l).build()));
    }

}
