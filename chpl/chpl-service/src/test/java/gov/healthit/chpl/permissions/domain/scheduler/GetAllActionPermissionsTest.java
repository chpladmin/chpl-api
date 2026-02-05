package gov.healthit.chpl.permissions.domain.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.JobDataMap;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllActionPermissions;

public class GetAllActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao;

    @Mock
    private CertifiedProductDAO certifiedProductDao;

    @Mock
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @InjectMocks
    private GetAllActionPermissions permissions;

    @BeforeEach
    public void setup() {
        permissions = new GetAllActionPermissions(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
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
        assertFalse(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ADMIN)));
        assertTrue(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ADMIN + ";" + CognitoGroups.CHPL_ONC)));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertTrue(permissions.hasAccess());
        assertFalse(permissions.hasAccess(buildChplJob()));
        assertFalse(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ADMIN)));
        assertFalse(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ADMIN + ";" + CognitoGroups.CHPL_ONC)));
        assertTrue(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ACB)));
        assertTrue(permissions.hasAccess(buildJob(CognitoGroups.CHPL_ONC + ";" + CognitoGroups.CHPL_ACB)));
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
