package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.manager.ApiKeyManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ApiKeyManagerTest extends TestCase {
	@Autowired
	private ApiKeyManager apiKeyManager;
	
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
	public void testCreateKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testDeleteKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		
		assertEquals(null, apiKeyManager.findKey(created.getId()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testDeleteByApiKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(toCreate.getApiKey(), created.getApiKey());
		assertEquals(toCreate.getApiKey(), retrieved.getApiKey());
		
		apiKeyManager.deleteKey(retrieved.getId());
		
		assertEquals(null, apiKeyManager.findKey(toCreate.getApiKey()));
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	
	@Test
	public void testFindByApiKey() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getApiKey());
		
		assertEquals(created.getApiKey(), retrieved.getApiKey());
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testFind() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		ApiKeyDTO retrieved = apiKeyManager.findKey(created.getId());
		
		assertEquals(created.getId(), retrieved.getId());
		apiKeyManager.deleteKey(retrieved.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	public void testFindAll() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		Integer countAll = apiKeyManager.findAll().size();
		
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		Integer newCount = apiKeyManager.findAll().size();
		
		assertEquals((int) newCount, (countAll + 1));
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
		
	}
	
	@Test
	public void TestLogApiKeyActivity() throws JsonProcessingException, EntityCreationException, EntityRetrievalException{
	
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetApiKeyActivity() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		int initialSize = apiKeyManager.getApiKeyActivity().size();
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		
		
		int finalSize = apiKeyManager.getApiKeyActivity().size();
		assertEquals((initialSize + 1), (finalSize));
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
	public void testGetApiKeyActivityByKey() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		int initialSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey()).size();
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal");
		
		int finalSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey()).size();
		apiKeyManager.deleteKey(created.getId());
		assertEquals((initialSize + 1), (finalSize));
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	// [region] Tests for getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli)
	/** Description: Tests that the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * returns a list of activities for the page equivalent to the pageSize
	 * Expected Result: number of activities in apiKeyActivityList matches the pageSize
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 * */
	@Test
	public void test_getApiKeyActivity_pageSize_numActivitiesMatchesPageSize() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal1");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal2");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal3");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal4");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal5");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal6");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal7");
		
		String apiKeyFilter = "";
		int pageNumber = 1;
		int pageSize = 2;
		boolean dateAscending = true;
		Long startTime = new Date(0).getTime();
		Long endTime = now.getTime();
		
		int finalSize = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startTime, endTime).size();
		assertEquals(pageSize, finalSize);
		
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	/** Description: Tests the apiKeyFilter parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in 'validAPIKey'. 
	 * Expected Result: Should return only API key activities with an API Key that matches the apiKeyFilter
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_apiKeyFilter_filtersResults() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		
		// Simulate API inputs
		String apiKeyFilter = "d334d18ed41f028f953cba15154700a4"; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		
		for(ApiKeyActivity activity : activitiesList){
			String activityApiKey = activity.getApiKey();
			assertEquals("The API Key Filter did not filter out API keys other than the specified key", apiKeyFilter, activityApiKey);
		}
	}
	
	/** Description: Tests the apiKeyFilter parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in '!validAPIKey'. 
	 * Expected Result: Should return only API key activities with an API Key that does NOT match the apiKeyFilter
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_apiKeyFilter_withExclamationFiltersResults() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		
		// Simulate API inputs
		String apiKeyFilter = "!d334d18ed41f028f953cba15154700a4"; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		
		String apiKeyToFilterOut = apiKeyFilter.substring(1);
		
		for(ApiKeyActivity activity : activitiesList){
			String activityApiKey = activity.getApiKey();
			assertNotSame("The API Key Filter did not filter out the API key", apiKeyToFilterOut, activityApiKey);
		}
	}
	
	/** Description: Tests the apiKeyFilter parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in '' (blank). 
	 * Expected Result: Should return all API key activities
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_apiKeyFilter_blankReturnsAllResults() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		
		// Simulate API inputs
		String apiKeyFilter = ""; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		assertTrue(apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli).size() == pageSize);
	}
	
	/** Description: Tests the apiKeyFilter parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in null. 
	 * Expected Result: Should return all API key activities
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_apiKeyFilter_nullReturnsAllResults() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		assertTrue(apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli).size() == pageSize);
	}
	
	/** Description: Tests the apiKeyFilter parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in '!' without an API key. 
	 * Expected Result: All API key activity results
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_apiKeyFilter_exclamationWithoutApiKeyReturnsAllResults() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		
		// Simulate API inputs
		String apiKeyFilter = "!"; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		assertTrue(apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli).size() == pageSize);
	}
	
	/** Description: Tests the dateAscending parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in &dateAscending=true
	 * Expected Result: All API key activities are returned in ascending order based on creation_date (oldest date to newest date)
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_dateAscending_trueReturnsResultsAsOldestToNewest() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		Long previousActivityTime = null;
		Long currentActivityTime = null;
		int counter = 0;
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		
		for(ApiKeyActivity activity : activitiesList){
			currentActivityTime = activity.getCreationDate().getTime();
			if(counter > 0){
				assertTrue("Activities are not listed in ascending order", currentActivityTime > previousActivityTime);
			}
			previousActivityTime = currentActivityTime;
			counter++;
		}
	}
	
	/** Description: Tests the dateAscending parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in &dateAscending=false
	 * Expected Result: All API key activities are returned in descending order based on creation_date (newest date to oldest date)
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_dateAscending_falseReturnsResultsAsNewestToOldest() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		Long previousActivityTime = null;
		Long currentActivityTime = null;
		int counter = 0;
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = false;
		long startDateMilli = 0; // beginning of time
		long endDateMilli = now.getTime(); // current time
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		
		for(ApiKeyActivity activity : activitiesList){
			currentActivityTime = activity.getCreationDate().getTime();
			if(counter > 0){
				assertTrue("Activities are not listed in descending order", previousActivityTime > currentActivityTime);
			}
			previousActivityTime = currentActivityTime;
			counter++;
		}
	}
	
	/** Description: Tests the startDate parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in &startDate=(value in milli of an API key activity creation_date where an activity with an older creation_date exists)
	 * Expected Result: Only API key activities are returned that have a creation_date >= startDate
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_startDate_noResultsPriorToStartDate() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Date now = new Date();
		Long currentActivityTime = null;
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		// startDateMilli = milli time of 2016/07/27 10:46:51 that is > creation_date of some API key activities 
		// and < creation_date of other API key activities
		long startDateMilli = 1469630811000L; 
		long endDateMilli = now.getTime(); // current time
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		assertTrue("Activities list should contain some activities", activitiesList.size() > 0);
		
		for(ApiKeyActivity activity : activitiesList){
			currentActivityTime = activity.getCreationDate().getTime();

			assertTrue("An activity with a creation_date < startDate should not be allowed", startDateMilli <= currentActivityTime);
		}
	}
	
	/** Description: Tests the startDate parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in &startDate=(value in milli of an API key activity creation_date) and &endDate = same value as startDate
	 * Expected Result: Only API key activities are returned that have a creation_date >= startDate
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_startDate_ReturnsApiKeyActivityWithCreationDateEqualToStartDate() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Long currentActivityTime = null;
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 1470757666349L; 
		long endDateMilli = startDateMilli;
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		assertTrue("Activities list should contain one activity", activitiesList.size() == 1);
		
		for(ApiKeyActivity activity : activitiesList){
			currentActivityTime = activity.getCreationDate().getTime();

			assertTrue("An activity with a creation_date == startDate could not be found", startDateMilli == currentActivityTime);
		}
	}
	
	/** Description: Tests the endDate parameter in the 
	 * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) 
	 * method when passing in &endDate=(value in milli of an API key activity creation_date where there are other API keys with an earlier creation_date)
	 * Expected Result: Only API key activities are returned that have a creation_date <= endDate
	 * Assumptions:
	 * An API key activity exists with creation_date <= the value of endDateMilli
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Test
	public void test_getApiKeyActivity_endDate_ReturnsApiKeyActivitiesWithCreationDateLessThanOrEqualToEndDate() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Long currentActivityTime = null;
		
		// Simulate API inputs
		String apiKeyFilter = null; // Valid API Key in openchpl_test DB
		Integer pageNumber = 0;
		Integer pageSize = 100;
		boolean dateAscending = true;
		long startDateMilli = 0; 
		long endDateMilli = 1470757666349L;
		
		List<ApiKeyActivity> activitiesList = apiKeyManager.getApiKeyActivity
				(apiKeyFilter, pageNumber, pageSize, dateAscending, startDateMilli, endDateMilli);
		assertTrue("Activities list should contain some activities", activitiesList.size() > 0);
		
		for(ApiKeyActivity activity : activitiesList){
			currentActivityTime = activity.getCreationDate().getTime();
			assertTrue("An activity cannot have a creation_date > the endDate", currentActivityTime <= endDateMilli);
		}
	}
	// [endRegion]
	
	@Test
	public void testGetApiKeyActivityWithKeyAndPaging() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		
		ApiKeyDTO toCreate = new ApiKeyDTO();
		Date now = new Date();
		
		toCreate.setEmail("test@test.com");
		toCreate.setNameOrganization("Ai");
		toCreate.setCreationDate(now);
		toCreate.setLastModifiedDate(now);
		toCreate.setLastModifiedUser(-3L);
		toCreate.setDeleted(false);
		
		String apiKey = gov.healthit.chpl.Util.md5(toCreate.getNameOrganization() + toCreate.getEmail() + now.getTime() );
		toCreate.setApiKey(apiKey);
		
		// create key
		ApiKeyDTO created = apiKeyManager.createKey(toCreate);
		
		// log activity for key
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal1");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal2");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal3");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal4");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal5");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal6");
		apiKeyManager.logApiKeyActivity(toCreate.getApiKey(), "/rest/some/call?someQuery=someVal7");	
		
		int pageSize = 2;
		int pageNumber = 1;
		int finalSize = apiKeyManager.getApiKeyActivity(toCreate.getApiKey(), pageNumber, pageSize).size();
		assertEquals((int) pageSize, finalSize);
		apiKeyManager.deleteKey(created.getId());
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
}