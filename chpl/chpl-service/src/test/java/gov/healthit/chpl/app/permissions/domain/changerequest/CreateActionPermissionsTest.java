package gov.healthit.chpl.app.permissions.domain.changerequest;

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
import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.DeveloperBuilder;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domains.changerequest.CreateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class CreateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllDevelopersForCurrentUser()).thenReturn(getAllDeveloperForUser(2l, 4l));
    }

    @Override
    @Test
    public void hasAccess_Developer() throws Exception {
        setupForDeveloperUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess(new ChangeRequestBuilder()
                .withDeveloper(new DeveloperBuilder().withId(2l).build())
                .build()));

        assertFalse(permissions.hasAccess(new ChangeRequestBuilder()
                .withDeveloper(new DeveloperBuilder().withId(3l).build())
                .build()));

    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequest()));
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
}
