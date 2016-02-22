package gov.healthit.chpl.dao.impl;


import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;



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
	
	
	private static JWTAuthenticatedUser adminUser;
	
	
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
		assertEquals(5, results.size());
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
	public void testFindAllInLastNDays() throws EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		List<ActivityDTO> results = activityDAO.findAllInLastNDays(false, 5);
		assertEquals(0,results.size());
		
		List<ActivityDTO> results2 = activityDAO.findAllInLastNDays(false, 50000);
		assertEquals(5 ,results2.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findAllInLastNDays(false, 5);
		assertEquals(1, results3.size());
		activityDAO.delete(created.getId());
		ActivityDTO deleted = activityDAO.getById(created.getId());
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	@Transactional
	public void testFindByObjectIdInLastNDays() throws EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		List<ActivityDTO> results = activityDAO.findByObjectId(false, 1L, ActivityConcept.ACTIVITY_CONCEPT_ATL, 5);
		assertEquals(0,results.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findByObjectId(false, created.getActivityObjectId(), ActivityConcept.ACTIVITY_CONCEPT_ATL, 5);
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
		
		List<ActivityDTO> results = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_ATL, 5);
		assertEquals(0,results.size());
		
		ActivityDTO recent = new ActivityDTO();
		recent.setActivityDate(new Date());
		recent.setActivityObjectId(100L);
		recent.setConcept(ActivityConcept.ACTIVITY_CONCEPT_ATL);
		recent.setDescription("Description");
		recent.setOriginalData("Original");
		recent.setNewData("New");
		
		ActivityDTO created = activityDAO.create(recent);
		
		List<ActivityDTO> results3 = activityDAO.findByConcept(false, ActivityConcept.ACTIVITY_CONCEPT_ATL, 5);
		assertEquals(1, results3.size());
		activityDAO.delete(created.getId());
		ActivityDTO deleted = activityDAO.getById(created.getId());
		assertNull(deleted);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
}
