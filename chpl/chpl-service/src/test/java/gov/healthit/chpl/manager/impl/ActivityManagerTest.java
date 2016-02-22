package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.ActivityManager;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


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
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
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
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testGetAllActivity() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = activityManager.getAllActivity(false);
		assertEquals(5, events.size());
		
	}
	
	
	@Test
	public void testGetAllActivityInLastNDays() throws EntityCreationException, EntityRetrievalException, IOException{
		
		Integer lastNDays = 5;
		
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
		List<ActivityEvent> events = activityManager.getAllActivityInLastNDays(false, lastNDays);
		
		activityManager.deleteActivity(events.get(0).getId());
		assertEquals(1, events.size());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
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
	public void testGetActivityForObjectLastNDays() throws EntityCreationException, EntityRetrievalException, IOException{
		
		Integer lastNDays = 5;
		
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
		List<ActivityEvent> events = activityManager.getActivityForObject(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, developer.getId() , lastNDays);
		
		activityManager.deleteActivity(events.get(0).getId());
		assertEquals(1, events.size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetActivityForConcept() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT);
		assertEquals(4, events.size());
		
		List<ActivityEvent> events2 = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_PRODUCT);
		assertEquals(1, events2.size());
		
		List<ActivityEvent> events3 = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER);
		assertEquals(0, events3.size());
		
	}
	
	@Test
	public void testGetActivityForConceptLastNDays() throws EntityCreationException, EntityRetrievalException, JsonParseException, IOException{
		
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		Integer lastNDays = 5;
		List<ActivityEvent> events = activityManager.getActivityForConcept(false, concept, lastNDays);
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
		
		List<ActivityEvent> events2 = activityManager.getActivityForConcept(false, ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, lastNDays);
		
		assertEquals(1, events2.size());
	}
	
	@Test
	public void testGetActivityForUser() throws JsonParseException, IOException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<ActivityEvent> eventsForUser = activityManager.getActivityForUser(1L);
		assertEquals(5, eventsForUser.size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
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
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
}
