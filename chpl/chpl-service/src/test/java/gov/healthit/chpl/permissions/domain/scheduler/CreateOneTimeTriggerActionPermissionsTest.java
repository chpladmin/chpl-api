package gov.healthit.chpl.permissions.domain.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.scheduler.CreateOneTimeTriggerActionPermissions;
import gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob;

public class CreateOneTimeTriggerActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateOneTimeTriggerActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

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
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

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
    public void hasAccess_Atl() throws Exception {
        setupForAtlUser(resourcePermissions);

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
    public void hasAccess_StartupUser_cacheRefreshJob() throws Exception {
        setupForStartupUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        trigger.setJob(ChplJob.builder()
                .name(DirectReviewCacheRefreshJob.JOB_NAME)
                .group(DirectReviewCacheRefreshJob.JOB_GROUP)
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
