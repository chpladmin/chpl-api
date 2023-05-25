package gov.healthit.chpl.permissions.domain.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllActionPermissions;

public class GetAllActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private GetAllActionPermissions permissions;

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
        assertTrue(permissions.hasAccess(buildChplJob()));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN")));
        assertTrue(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC")));
        assertTrue(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC_STAFF;ROLE_ONC")));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN")));
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC")));
        assertTrue(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC_STAFF;ROLE_ONC")));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildChplJob()));
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN")));
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC")));
        assertFalse(permissions.hasAccess(buildJob("ROLE_ADMIN;ROLE_ONC_STAFF;ROLE_ONC")));
        assertTrue(permissions.hasAccess(buildJob("ROLE_ACB")));
        assertTrue(permissions.hasAccess(buildJob("ROLE_ONC;ROLE_ACB")));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildChplJob()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildChplJob()));
    }

    private ChplJob buildChplJob() {
        return ChplJob.builder()
                .group(SchedulerManager.CHPL_JOBS_KEY)
                .build();
    }

    private ChplJob buildJob(String delimitedAuthorities) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("authorities", delimitedAuthorities);
        return ChplJob.builder()
                .group(SchedulerManager.CHPL_JOBS_KEY)
                .jobDataMap(jobDataMap)
                .build();
    }
}
