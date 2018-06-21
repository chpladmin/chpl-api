package gov.healthit.chpl.manager.impl;

import java.io.IOException;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.ProductActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ActivityManagerTest extends TestCase {
	@Autowired
	private ActivityManager activityManager;
	
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
	public void testAddActivity() throws EntityCreationException, EntityRetrievalException, IOException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setId(-1L);
		developer.setName("Test");
		developer.setWebsite("www.zombo.com");
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer.getId(), 
				"Test Activity",
				null,
				developer
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER);
		assertEquals(event.getOriginalData(), null);
		assertEquals(event.getNewData().toString(), JSONUtils.toJSON(developer));
		assertEquals(event.getActivityObjectId(), developer.getId());
		
		activityManager.deleteActivity(event.getId());
	}
	
	
	@Test
	@Transactional
	@Rollback
	public void testAddActivityWithTimeStamp() throws EntityCreationException, EntityRetrievalException, IOException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setId(-1L);
		developer.setName("Test");
		developer.setWebsite("www.zombo.com");
		
		Date timestamp = new Date();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer.getId(), 
				"Test Activity",
				null,
				developer,
				timestamp
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER);
		assertEquals(event.getOriginalData(), null);
		assertEquals(event.getNewData().toString(), JSONUtils.toJSON(developer));
		assertEquals(event.getActivityObjectId(), developer.getId());
		//assertEquals(event.getActivityDate(), timestamp);
		
		activityManager.deleteActivity(event.getId());
	}
	
	
	@Test
	@Transactional
	public void testGetAllActivity() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = activityManager.getAllActivity(false);
		assertEquals(7, events.size());
		
	}
	
	
	@Test
	@Transactional
	public void testGetAllActivityInLastNDays() throws EntityCreationException, EntityRetrievalException, IOException{
		Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5*24*60*60*1000));
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setId(1L);
		developer.setName("Test");
		developer.setWebsite("www.zombo.com");
		Date timestamp = new Date();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer.getId(),
				"Test Activity",
				"Before",
				"Test",
				timestamp
				);
		List<ActivityEvent> events = activityManager.getAllActivityInDateRange(false, fiveDaysAgo, null);
		assertEquals(1, events.size());
		activityManager.deleteActivity(events.get(0).getId());
		events = activityManager.getAllActivityInDateRange(false, fiveDaysAgo, null);
		assertEquals(0, events.size());
	}
	
	@Test
	@Transactional
	public void testGetActivityForObject() throws JsonParseException, IOException{
		
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		Long objectId = 1L;
		
		List<ActivityEvent> events = activityManager.getActivityForObject(false, concept, objectId);
		assertEquals(4, events.size());
		
		for (ActivityEvent event : events){
			assertEquals(concept, event.getConcept());
			assertEquals(objectId, event.getActivityObjectId());
		}
		
	}
	
	@Test
	@Transactional
	public void testGetActivityForObjectDateRange() throws EntityCreationException, EntityRetrievalException, IOException{
		
		Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5*24*60*60*1000));
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setId(1L);
		developer.setName("Test");
		developer.setWebsite("www.zombo.com");
		
		Date timestamp = new Date();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer.getId(),
				"Test Activity",
				"Before",
				"Test",
				timestamp
				);
		List<ActivityEvent> events = activityManager.getActivityForObject(false, 
				ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, developer.getId() , 
				fiveDaysAgo, null);
		
		activityManager.deleteActivity(events.get(0).getId());
		assertEquals(1, events.size());
	}
	
	@Test
	@Transactional
	public void testGetActivityForConcept() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		assertEquals(4, events.size());
		
		List<ActivityEvent> events2 = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_PRODUCT);
		assertEquals(3, events2.size());
		
		List<ActivityEvent> events3 = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER);
		assertEquals(0, events3.size());
		
		List<ActivityEvent> capEvents = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_CORRECTIVE_ACTION_PLAN);
        assertEquals(0, capEvents.size());
	}
	
	@Test
	@Transactional
	public void testGetActivityForConcept_developerNameIsFetched() throws JsonParseException, IOException{
		List<ActivityEvent> events = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_PRODUCT);
		assertEquals(3, events.size());
		assertTrue(events.get(0) instanceof ProductActivityEvent);
		ProductActivityEvent myEvent = null;
		for(ActivityEvent event : events){
			if(event.getId().equals(-5L)){
				myEvent = (ProductActivityEvent) event;
			}
		}
		assertNotNull(myEvent.getDeveloper());
		assertNotNull(myEvent.getDeveloper().getName());
		assertEquals("Test Developer 1", myEvent.getDeveloper().getName());
	}
	
	@Test
	@Transactional
	public void testGetActivityForConceptLastNDays() throws EntityCreationException, EntityRetrievalException, JsonParseException, IOException{
		
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		Date fiveDaysAgo = new Date(System.currentTimeMillis() - (5*24*60*60*1000));

		List<ActivityEvent> events = activityManager.getActivityForConcept(false, concept, fiveDaysAgo, null);
		assertEquals(0, events.size());
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		DeveloperDTO developer = new DeveloperDTO();
		developer.setCreationDate(new Date());
		developer.setId(1L);
		developer.setName("Test");
		developer.setWebsite("www.zombo.com");
		
		Date timestamp = new Date();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer.getId(),
				"Test Activity",
				"Before",
				"Test",
				timestamp
				);
		
		DeveloperDTO developer2 = new DeveloperDTO();
		developer2.setCreationDate(new Date());
		developer2.setId(2L);
		developer2.setName("Test");
		developer2.setWebsite("www.zombo.com");
		
		Date timestamp2 = new Date(100);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER,
				developer2.getId(),
				"Test Activity",
				"Before",
				"Test",
				timestamp2
				);
		
		List<ActivityEvent> events2 = activityManager.getActivityForConcept(false, 
				ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, fiveDaysAgo, new Date());
		
		assertEquals(1, events2.size());
	}
	
	@Test
	@Transactional
	public void testGetActivityForUser() throws JsonParseException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<ActivityEvent> eventsForUser = activityManager.getActivityForUser(1L);
		assertEquals(5, eventsForUser.size());
	}
	
	@Test
	@Transactional
	public void testGetActivityByUser() throws JsonParseException, IOException, UserRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<UserActivity> eventsForUser = activityManager.getActivityByUser();
		
		List<UserActivity> forUser = new ArrayList<UserActivity>();
		
		for (UserActivity activity : eventsForUser){
			if(activity.getUser().getUserId().equals(1L)){
				forUser.add(activity);
			}
		}
		assertEquals(5, forUser.get(0).getEvents().size());
	}
	
	/**
	 * Given the API call is made for /activity/user_activities?start=milliValue?end=milliValue
	 * When an activity exists for a valid user
	 * Then the authenticated user's activity is returned
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws UserRetrievalException
	 */
	@Test
	@Transactional
	public void testFindAllByUserInDateRange_returnsResult() throws JsonParseException, IOException, UserRetrievalException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date startDate = new Date(1489550400000L); // 2017-03-15
		Date endDate = new Date(1489723200000L); // 2017-03-17
		// Create a user activity for a user that does not exist
		List<UserActivity> eventsForUser = activityManager.getActivityByUserInDateRange(startDate, endDate);
		
		List<UserActivity> forUser = new ArrayList<UserActivity>();
		
		for (UserActivity activity : eventsForUser){
			if(activity.getUser().getUserId().equals(-2L)){
				forUser.add(activity);
			}
		}
		assertEquals(1, forUser.get(0).getEvents().size());
	}
	
}
