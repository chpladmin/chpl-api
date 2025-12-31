package gov.healthit.chpl.permissions.domain.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.scheduler.CreateOneTimeTriggerActionPermissions;
import gov.healthit.chpl.scheduler.job.CognitoUserCacheRefreshJob;
import gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob;

public class CreateOneTimeTriggerActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private CreateOneTimeTriggerActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new CreateOneTimeTriggerActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertTrue(permissions.hasAccess(new ChplOneTimeTrigger()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChplOneTimeTrigger()));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChplOneTimeTrigger()));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChplOneTimeTrigger()));
    }

    @Test
    public void hasAccess_StartupUser_emptyTrigger() throws Exception {
        setupForStartupUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChplOneTimeTrigger()));
    }

    @Test
    public void hasAccess_StartupUser_directReviewCacheRefreshJob() throws Exception {
        setupForStartupUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        trigger.setJob(ChplJob.builder()
                .name(DirectReviewCacheRefreshJob.JOB_NAME)
                .group(DirectReviewCacheRefreshJob.JOB_GROUP)
                .build());
        assertTrue(permissions.hasAccess(trigger));
    }

    @Test
    public void hasAccess_StartupUser_cognitoUserCacheRefreshJob() throws Exception {
        setupForStartupUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        trigger.setJob(ChplJob.builder()
                .name(CognitoUserCacheRefreshJob.JOB_NAME)
                .group(CognitoUserCacheRefreshJob.JOB_GROUP)
                .build());
        assertTrue(permissions.hasAccess(trigger));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new ChplOneTimeTrigger()));
    }
}
