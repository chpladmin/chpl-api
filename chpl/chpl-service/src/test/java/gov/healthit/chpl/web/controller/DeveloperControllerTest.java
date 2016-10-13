package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import net.sf.ehcache.CacheManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class DeveloperControllerTest {
	@Autowired
	DeveloperController developerController = new DeveloperController();
	
	@Autowired
	SearchViewController searchViewController = new SearchViewController();
	
	@Autowired
	CacheManager cacheManager = CacheManager.getInstance();

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
	
	/** Description: Tests that the updateDevelopers API call triggers the searchOptionsCache to evict/refresh
	 * 
	 * Expected Result: Executing the updateDevelopers method should cause the searchOptions cache to be refreshed
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_updateDevelopers_triggersSearchOptionsCacheEvict() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		cacheManager.clearAll();
		long startTime = System.currentTimeMillis();
		Boolean required = true;
		// Cache SearchOptions
		searchViewController.getPopulateSearchData(required);
		long endTime = System.currentTimeMillis();
		long timeLength = endTime - startTime;
		double elapsedSeconds = timeLength / 1000.0;
		assertTrue("search options should take longer than 100 ms to call the first time but took " + elapsedSeconds + 
				" and " + timeLength + "ms", timeLength > 100L);
		
		// Verify that SearchOptions is cached
		startTime = System.currentTimeMillis();
		searchViewController.getPopulateSearchData(required);
		endTime = System.currentTimeMillis();
		timeLength = endTime - startTime;
		elapsedSeconds = timeLength / 1000.0;
		assertTrue("search options should now be cached and take < 100 ms but took " + timeLength + "ms", timeLength < 100L);
		
		Developer developer = new Developer();
		DeveloperStatus developerStatus = new DeveloperStatus();
		developerStatus.setId(1L);
		developerStatus.setStatus("Active");
		developer.setDeveloperId(-1L);
		developer.setStatus(developerStatus);
		
		UpdateDevelopersRequest updateDevelopersRequest = new UpdateDevelopersRequest();
		updateDevelopersRequest.setDeveloper(developer);
		List<Long> developerIds = new ArrayList<Long>();
		developerIds.add(-1L);
		updateDevelopersRequest.setDeveloperIds(developerIds);
		
		// Evict SearchOptions cache and verify that cache is cleared
		developerController.updateDeveloper(updateDevelopersRequest);
		
		startTime = System.currentTimeMillis();
		searchViewController.getPopulateSearchData(required);
		endTime = System.currentTimeMillis();
		timeLength = endTime - startTime;
		elapsedSeconds = timeLength / 1000.0;
		assertTrue("search options should no longer be cached but took " + timeLength + "ms", timeLength > 100L);
	}
	
}