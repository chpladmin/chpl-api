package gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class JobDaoTest extends TestCase {

    @Autowired
    private JobDAO jobDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testAddJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = jobDao.getById(job.getId());
        assertNotNull(job.getJobType());
        assertEquals(job.getJobType().getId().longValue(), jobType.getId().longValue());
        assertNotNull(job.getUser());
        assertEquals(job.getUser().getId().longValue(), user.getId().longValue());
        assertEquals(data, job.getData());
        assertNull(job.getStartTime());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testCreateAndStartJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = jobDao.getById(job.getId());
        assertNotNull(job.getJobType());
        assertEquals(job.getJobType().getId().longValue(), jobType.getId().longValue());
        assertNotNull(job.getUser());
        assertEquals(job.getUser().getId().longValue(), user.getId().longValue());
        assertEquals(data, job.getData());
        assertNull(job.getStartTime());

        try {
            jobDao.markStarted(job);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testCreateAndStartJobWithMessage() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = jobDao.getById(job.getId());
        assertNotNull(job.getJobType());
        assertEquals(job.getJobType().getId().longValue(), jobType.getId().longValue());
        assertNotNull(job.getUser());
        assertEquals(job.getUser().getId().longValue(), user.getId().longValue());
        assertEquals(data, job.getData());
        assertNull(job.getStartTime());

        try {
            jobDao.markStarted(job);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        jobDao.addJobMessage(job, "A message!");
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindAllJobs() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        List<JobDTO> allJobs = jobDao.findAll();
        assertEquals(1, allJobs.size());
        assertNotNull(allJobs.get(0));
        assertNotNull(allJobs.get(0).getId());
        assertEquals(job.getId().longValue(), allJobs.get(0).getId().longValue());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindRunningJobs() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = new JobDTO();
        job.setJobType(jobType);
        job.setUser(user);
        job.setData(data);
        job.setStartTime(startTime);
        job.setEndTime(new Date());

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        List<JobDTO> jobs = jobDao.findAllRunning();
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindRunningJobsBetweenDatesNoCompletedJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = new JobDTO();
        job.setJobType(jobType);
        job.setUser(user);
        job.setData(data);
        Date oldStartTime = new Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000);
        job.setStartTime(oldStartTime);
        Date oldEndTime = new Date(System.currentTimeMillis() - 14 * 20 * 60 * 60 * 1000);
        job.setEndTime(oldEndTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        try {
            jobDao.delete(job.getId());
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        Date searchStartTime = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
        List<JobDTO> jobs = jobDao.findAllRunningAndCompletedBetweenDates(searchStartTime, new Date(), null);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindRunningJobsBetweenDatesWithCompletedJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        job = new JobDTO();
        job.setJobType(jobType);
        job.setUser(user);
        job.setData(data);
        Date oldStartTime = new Date(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000);
        job.setStartTime(oldStartTime);
        Date oldEndTime = new Date(System.currentTimeMillis() - 4 * 20 * 60 * 60 * 1000);
        job.setEndTime(oldEndTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        try {
            jobDao.delete(job.getId());
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        Date searchStartTime = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
        List<JobDTO> jobs = jobDao.findAllRunningAndCompletedBetweenDates(searchStartTime, new Date(), null);
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindJobsByUserWithJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        List<JobDTO> allJobs = jobDao.getByUser(user.getId());
        assertEquals(1, allJobs.size());
        assertNotNull(allJobs.get(0));
        assertNotNull(allJobs.get(0).getId());
        assertEquals(job.getId().longValue(), allJobs.get(0).getId().longValue());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testFindJobsByUserWithoutJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        List<JobDTO> allJobs = jobDao.getByUser(-4L);
        assertEquals(0, allJobs.size());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testUpdateJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        Date endTime = new Date(System.currentTimeMillis() + 1000);
        job.setEndTime(endTime);
        try {
            jobDao.update(job);
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        job = jobDao.getById(job.getId());
        assertNotNull(job.getJobType());
        assertEquals(job.getJobType().getId().longValue(), jobType.getId().longValue());
        assertNotNull(job.getUser());
        assertEquals(job.getUser().getId().longValue(), user.getId().longValue());
        assertEquals(data, job.getData());
        assertNotNull(job.getStartTime());
        assertEquals(startTime.getTime(), job.getStartTime().getTime());
        assertNotNull(job.getEndTime());
        assertEquals(endTime.getTime(), job.getEndTime().getTime());
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testDeleteJob() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        JobDTO job = new JobDTO();
        JobTypeDTO jobType = new JobTypeDTO();
        jobType.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(adminUser.getId());
        job.setJobType(jobType);
        job.setUser(user);
        String data = "Some,CSV,Data";
        job.setData(data);
        Date startTime = new Date();
        job.setStartTime(startTime);

        try {
            job = jobDao.create(job);
        } catch (EntityCreationException ex) {
            fail(ex.getMessage());
        }

        assertNotNull(job);
        assertNotNull(job.getId());
        assertTrue(job.getId() > 0);

        try {
            jobDao.delete(job.getId());
        } catch (EntityRetrievalException ex) {
            fail(ex.getMessage());
        }

        job = jobDao.getById(job.getId());
        assertNull(job);
    }
}
