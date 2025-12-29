package gov.healthit.chpl.permissions.domain.changerequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;

public class UpdateActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Mock
    private ChangeRequestDAO changeRequestDAO;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @BeforeEach
    public void setup() throws EntityRetrievalException {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.getAllDevelopersForCurrentUser()).thenReturn(getAllDeveloperForUser(2L, 4L));

        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().id(2L).build())
                        .build());
    }

    @Override
    @Test
    @Disabled
    public void hasAccess_Developer() throws Exception {
        setupForDeveloperUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().id(2L).build())
                        .build());

        assertTrue(permissions.hasAccess(ChangeRequestUpdateRequest.builder()
                    .changeRequest(ChangeRequest.builder()
                                .developer(Developer.builder().id(2L).build())
                                .build())
                    .acknowledgeWarnings(false)
                    .build()));

        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequest.builder()
                        .developer(Developer.builder().id(3L).build())
                        .build());

        assertFalse(permissions.hasAccess(ChangeRequestUpdateRequest.builder()
                    .changeRequest(ChangeRequest.builder()
                            .developer(Developer.builder().id(3L).build())
                            .build())
                    .acknowledgeWarnings(false)
                    .build()));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ChangeRequestUpdateRequest()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ChangeRequestUpdateRequest()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        ChangeRequest changeRequest = ChangeRequest.builder()
                .id(1L)
                .certificationBodies(Stream.of(CertificationBody.builder().id(1L).build()).toList())
                .build();

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(1L));
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
            .thenReturn(changeRequest);

        assertTrue(permissions.hasAccess(ChangeRequestUpdateRequest.builder()
                .changeRequest(changeRequest)
                .acknowledgeWarnings(false)
                .build()));

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
                .thenReturn(getAllAcbForUser(5L));
        assertFalse(permissions.hasAccess(ChangeRequestUpdateRequest.builder()
                .changeRequest(changeRequest)
                .acknowledgeWarnings(false)
                .build()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequestUpdateRequest()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);
        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChangeRequestUpdateRequest()));

    }

    private List<CertificationBody> getDeveloperAcbs() {
        return new ArrayList<CertificationBody>(Arrays.asList(
                CertificationBody.builder().id(1L).build(),
                CertificationBody.builder().id(2L).build()));
    }

}
