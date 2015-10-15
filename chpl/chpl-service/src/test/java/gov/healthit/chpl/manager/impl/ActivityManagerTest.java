package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.dto.VendorDTO;
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

import com.fasterxml.jackson.core.JsonProcessingException;
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
	public void testAddActivity() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setId(-1L);
		vendor.setName("Test");
		vendor.setWebsite("www.zombo.com");
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR,
				vendor.getId(), 
				"Test Activity",
				null,
				vendor
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_VENDOR);
		assertEquals(event.getOriginalData(), null);
		assertEquals(event.getNewData(), JSONUtils.toJSON(vendor));
		assertEquals(event.getActivityObjectId(), vendor.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testAddActivityWithTimeStamp() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setId(-1L);
		vendor.setName("Test");
		vendor.setWebsite("www.zombo.com");
		
		Date timestamp = new Date();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR,
				vendor.getId(), 
				"Test Activity",
				null,
				vendor,
				timestamp
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_VENDOR);
		assertEquals(event.getOriginalData(), null);
		assertEquals(event.getNewData(), JSONUtils.toJSON(vendor));
		assertEquals(event.getActivityObjectId(), vendor.getId());
		assertEquals(event.getActivityDate(), timestamp);
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testAddActivityWithString() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setId(-1L);
		vendor.setName("Test");
		vendor.setWebsite("www.zombo.com");
		
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR,
				vendor.getId(), 
				"Test Activity",
				"Before",
				"Test"
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_VENDOR);
		assertEquals(event.getOriginalData(), "Before");
		assertEquals(event.getNewData(), "Test");
		assertEquals(event.getActivityObjectId(), vendor.getId());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testAddActivityWithStringTimeStamp() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		VendorDTO vendor = new VendorDTO();
		vendor.setCreationDate(new Date());
		vendor.setId(-1L);
		vendor.setName("Test");
		vendor.setWebsite("www.zombo.com");
		
		Date timestamp = new Date();
		
		
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR,
				vendor.getId(),
				"Test Activity",
				"Before",
				"Test",
				timestamp
				);
		
		List<ActivityEvent> events = activityManager.getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, -1L);	
		
		ActivityEvent event = events.get(events.size()-1);
		
		assertEquals(event.getConcept(), ActivityConcept.ACTIVITY_CONCEPT_VENDOR);
		assertEquals(event.getOriginalData(), "Before");
		assertEquals(event.getNewData(), "Test");
		assertEquals(event.getActivityObjectId(), vendor.getId());
		assertEquals(event.getActivityDate(), timestamp);
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	
	/*
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, String originalData, String newData) throws EntityCreationException, EntityRetrievalException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, String originalData, String newData, Date timestamp) throws EntityCreationException, EntityRetrievalException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, Date timestamp) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public List<ActivityEvent> getAllActivity();
	public List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId);
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept);
	public List<ActivityEvent> getAllActivityInLastNDays(Integer lastNDays);
	public List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId, Integer lastNDays);
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Integer lastNDays);
	*/
}
