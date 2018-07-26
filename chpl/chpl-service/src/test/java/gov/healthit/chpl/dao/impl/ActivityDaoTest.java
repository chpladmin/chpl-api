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
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
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
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	@Transactional
	@Rollback
	public void testCreateActivity() throws EntityCreationException, EntityRetrievalException{
		
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
		
		activityDAO.delete(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	@Rollback
	public void testUpdateActivity() throws EntityCreationException, EntityRetrievalException{
		
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
		
		
		created.setActivityDate(new Date(100));
		created.setActivityObjectId(2L);
		created.setConcept(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		created.setDescription("Some more activity occurred");
		created.setOriginalData("Updated_Original");
		created.setNewData("Updated_New");
		
		activityDAO.update(created);
		
		ActivityDTO check = activityDAO.getById(created.getId());
		
		assertEquals(created.getDescription(), check.getDescription());
		assertEquals(created.getNewData(), check.getNewData());
		assertEquals(created.getOriginalData(), check.getOriginalData());
		assertEquals(created.getActivityDate(), check.getActivityDate());
		assertEquals(created.getActivityObjectId(), check.getActivityObjectId());
		assertEquals(created.getConcept(), check.getConcept());
		assertEquals(created.getId(), check.getId());
		
		activityDAO.delete(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	@Rollback
	public void testDelete() throws EntityCreationException, EntityRetrievalException{
		
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
		
		activityDAO.delete(created.getId());
		
		ActivityDTO deleted = activityDAO.getById(created.getId());
		
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
		
	}
	
	@Test
	@Transactional
	public void testGetById() throws EntityRetrievalException{
		ActivityDTO dto = activityDAO.getById(-1L);
		assertEquals((long) dto.getId(), -1L);
		
	}
	
	@Test
	@Transactional
	public void testFindAll(){
		
		List<ActivityDTO> results = activityDAO.findAll(false);
		assertEquals(7, results.size());
	}
	
	@Test
	@Transactional
	public void testFindOne_userExists(){
		
		List<ActivityDTO> results = activityDAO.findAll(false);
		assertTrue(results.size() > 0);
		ActivityDTO result = results.get(1);
		assertNotNull(result.getUser());
		assertNotNull(result.getUser().getFirstName());
		assertNotNull(result.getUser().getLastName());
	}
	
	@Test
	@Transactional
	public void testFindByObjectId(){
		
		List<ActivityDTO> results = activityDAO.findByObjectId(false, 1L, ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		assertEquals(4, results.size());
		
	}
	
	@Test
	@Transactional
	public void testFindByConcept(){
		
		List<ActivityDTO> results = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		assertEquals(4, results.size());
		for(ActivityDTO dto : results){
			assertEquals(dto.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		}
		
		List<ActivityDTO> developerResults = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER);
		assertEquals(0, developerResults.size());
		
	}
	
	@Test
    @Transactional
    public void testFindPublicAnnouncementActivityNativeSqlWorks(){
        
        List<ActivityDTO> results = activityDAO.findPublicAnnouncementActivity(new Date(0L), new Date());
        assertEquals(0, results.size());
    }
	
	@Test
    @Transactional
    public void testFindPublicAnnouncementByIdActivityNativeSqlWorks(){
        
        List<ActivityDTO> results = activityDAO.findPublicAnnouncementActivityById(1L, new Date(0L), new Date());
        assertEquals(0, results.size());
    }
	
	@Test
    @Transactional
    public void testFindAcbActivityNativeSqlWorks(){
        List<CertificationBodyDTO> allAcbs = acbDao.findAll(false);
        List<ActivityDTO> results = activityDAO.findAcbActivity(allAcbs, new Date(0L), new Date());
        assertEquals(0, results.size());
    }

	@Test
    @Transactional
    public void testFindAtlActivityNativeSqlWorks(){
        List<TestingLabDTO> atllAtls = atlDao.findAll(false);
        List<ActivityDTO> results = activityDAO.findAtlActivity(atllAtls, new Date(0L), new Date());
        assertEquals(0, results.size());
    }
	
	@Test
    @Transactional
    public void testFindPendingListingActivityNativeSqlWorks(){
        List<ActivityDTO> results = activityDAO.findPendingListingActivity(1L, new Date(0L), new Date());
        assertEquals(0, results.size());
    }
	
	@Test
    @Transactional
    public void testFindPendingListingActivityByAcbNativeSqlWorks(){
        List<CertificationBodyDTO> allAcbs = acbDao.findAll(false);
        List<ActivityDTO> results = activityDAO.findPendingListingActivity(allAcbs, new Date(0L), new Date());
        assertEquals(0, results.size());
    }
	
	@Test
	@Transactional
	public void testFindAllInLastNDays() throws EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date fiveDaysAgo = new Date(1489699376931L - (5*24*60*60*1000)); // 3/16/2017 in millis - 5 days in millis
		List<ActivityDTO> results = activityDAO.findAllInDateRange(false, fiveDaysAgo, new Date(1489699376931L));
		assertEquals(2,results.size());
		
		List<ActivityDTO> results2 = activityDAO.findAllInDateRange(false, null, new Date());
		assertEquals(7 ,results2.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findAllInDateRange(false, fiveDaysAgo, new Date());
		assertEquals(3, results3.size());
		activityDAO.delete(created.getId());
		ActivityDTO deleted = activityDAO.getById(created.getId());
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	public void testFindByObjectIdInLastNDays() throws EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5*24*60*60*1000));
		List<ActivityDTO> results = activityDAO.findByObjectId(false, 1L, ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, null);
		assertEquals(0,results.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findByObjectId(false, created.getActivityObjectId(), ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, null);
		assertEquals(1, results3.size());
		activityDAO.delete(created.getId());
		ActivityDTO deleted = activityDAO.getById(created.getId());
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	public void testFindByConceptInLastNDays() throws EntityCreationException, EntityRetrievalException{;
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5*24*60*60*1000));
		List<ActivityDTO> results = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, new Date());
		assertEquals(0,results.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_ATL, fiveDaysAgo, null);
		assertEquals(1, results3.size());
		activityDAO.delete(created.getId());
		ActivityDTO deleted = activityDAO.getById(created.getId());
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
}
