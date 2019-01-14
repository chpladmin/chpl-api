package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
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
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
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
public class ActivityDaoTest extends TestCase {

    @Autowired
    private ActivityDAO activityDAO;

    @Autowired
    private CertificationBodyDAO acbDao;

    @Autowired
    private TestingLabDAO atlDao;

    private static JWTAuthenticatedUser adminUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

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
    @Transactional
    @Rollback
    public void testCreateActivity() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);

        ActivityDTO dto = new ActivityDTO();
        dto.setActivityDate(new Date());
        dto.setActivityObjectId(1L);
        dto.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
        dto.setDeleted(false);
        dto.setDescription("Some activity occurred");
        dto.setOriginalData("Original");
        dto.setNewData("New");

        ActivityDTO created = activityDAO.create(dto);
        ActivityDTO check = activityDAO.getById(created.getId());

        assertEquals(created.getDescription(), check.getDescription());
        assertEquals(created.getNewData(), check.getNewData());
        assertEquals(created.getOriginalData(), check.getOriginalData());
        assertEquals(created.getActivityDate(), check.getActivityDate());
        assertEquals(created.getActivityObjectId(), check.getActivityObjectId());
        assertEquals(created.getConcept(), check.getConcept());
        assertEquals(created.getId(), check.getId());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    public void testGetById() throws EntityRetrievalException {
        ActivityDTO dto = activityDAO.getById(-1L);
        assertEquals((long) dto.getId(), -1L);

    }

    @Test
    @Transactional
    public void testFindPublicAnnouncementActivityNativeSqlWorks() {
        List<ActivityDTO> results = activityDAO.findPublicAnnouncementActivity(new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindPublicAnnouncementByIdActivityNativeSqlWorks() {
        List<ActivityDTO> results = activityDAO.findPublicAnnouncementActivityById(1L, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindAcbActivityNativeSqlWorks() {
        List<CertificationBodyDTO> allAcbs = acbDao.findAll();
        List<ActivityDTO> results = activityDAO.findAcbActivity(allAcbs, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindAtlActivityNativeSqlWorks() {
        List<TestingLabDTO> atllAtls = atlDao.findAll();
        List<ActivityDTO> results = activityDAO.findAtlActivity(atllAtls, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindPendingListingActivityNativeSqlWorks() {
        List<ActivityDTO> results = activityDAO.findPendingListingActivity(1L, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindPendingListingActivityByAcbNativeSqlWorks() {
        List<CertificationBodyDTO> allAcbs = acbDao.findAll();
        List<ActivityDTO> results = activityDAO.findPendingListingActivity(allAcbs, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testFindUserActivityNativeSqlWorks() {
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(-1L);
        userIds.add(-2L);
        List<ActivityDTO> results = activityDAO.findUserActivity(userIds, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testFindByObjectIdInLastNDays() throws EntityCreationException, EntityRetrievalException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000));
        List<ActivityDTO> results = activityDAO.findByObjectId(1L, ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo,
                null);
        assertEquals(0, results.size());

        ActivityDTO recent = new ActivityDTO();
        recent.setActivityDate(new Date());
        recent.setActivityObjectId(100L);
        recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
        recent.setDescription("Description");
        recent.setOriginalData("Original");
        recent.setNewData("New");

        ActivityDTO created = activityDAO.create(recent);

        List<ActivityDTO> results3 = activityDAO.findByObjectId(created.getActivityObjectId(),
                ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, null);
        assertEquals(1, results3.size());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @Transactional
    @Rollback
    public void testFindByConceptInLastNDays() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000));
        List<ActivityDTO> results = activityDAO.findByConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo,
                new Date());
        assertEquals(0, results.size());

        ActivityDTO recent = new ActivityDTO();
        recent.setActivityDate(new Date());
        recent.setActivityObjectId(100L);
        recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
        recent.setDescription("Description");
        recent.setOriginalData("Original");
        recent.setNewData("New");

        ActivityDTO created = activityDAO.create(recent);

        List<ActivityDTO> results3 = activityDAO.findByConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, null);
        assertEquals(1, results3.size());

        SecurityContextHolder.getContext().setAuthentication(null);

    }

}
