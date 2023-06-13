package gov.healthit.chpl.permissions.domain.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.scheduler.CreateBackgroundJobTriggerActionPermissions;
import gov.healthit.chpl.scheduler.job.RealWorldTestingUploadJob;
import gov.healthit.chpl.scheduler.job.SplitDeveloperJob;

public class CreateBackgroundJobTriggerActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CreateBackgroundJobTriggerActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        permissions.init();
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        //Not Used
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess("JOB NAME"));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        //Not Used
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess("JOB NAME"));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        //Not Used
        assertFalse(permissions.hasAccess());

        ChplOneTimeTrigger trigger = new ChplOneTimeTrigger();
        ChplJob job = new ChplJob();
        job.setName(RealWorldTestingUploadJob.JOB_NAME);
        trigger.setJob(job);
        assertTrue(permissions.hasAccess(trigger));

        job = new ChplJob();
        job.setName(SplitDeveloperJob.JOB_NAME);
        trigger.setJob(job);
        assertTrue(permissions.hasAccess(trigger));

        job = new ChplJob();
        job.setName("OTHER_JOB");
        trigger.setJob(job);
        assertFalse(permissions.hasAccess(trigger));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Object()));
    }

}
