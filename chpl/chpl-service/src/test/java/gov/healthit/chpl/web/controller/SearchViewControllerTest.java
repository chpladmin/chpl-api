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
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class SearchViewControllerTest {
	@Autowired
	SearchViewController searchViewController = new SearchViewController();

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
	
	/** Description: Tests that the advancedSearch returns valid SearchResponse records when refined by cqms
	 * 
	 * Expected Result: Completes without error and returns some SearchResponse records
	 */
	@Transactional
	@Rollback(true) 
	@Test
	public void test_advancedSearch_refineByCqms_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SearchRequest searchFilters = new SearchRequest();
		List<String> cqms = new ArrayList<String>();
		cqms.add("0001");
		searchFilters.setCqms(cqms);
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setOrderBy("developer");
		searchFilters.setSortDescending(true);
		
		SearchResponse searchResponse = new SearchResponse();
		searchResponse = searchViewController.advancedSearch(searchFilters);
		assertTrue("searchViewController.simpleSearch() should return a SearchResponse with records", searchResponse.getRecordCount() > 0);
	}
	
	/** Description: Tests that the advancedSearch returns valid SearchResponse records when refined by certification criteria
	 * 
	 * Expected Result: Completes without error and returns some SearchResponse records
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_advancedSearch_refineByCertificationCriteria_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SearchRequest searchFilters = new SearchRequest();
		List<String> certificationCriteria = new ArrayList<String>();
		certificationCriteria.add("170.315 (a)(1)");
		
		searchFilters.setCertificationCriteria(certificationCriteria);
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setOrderBy("developer");
		searchFilters.setSortDescending(true);
		
		SearchResponse searchResponse = new SearchResponse();
		searchResponse = searchViewController.advancedSearch(searchFilters);
		assertTrue("searchViewController.simpleSearch() should return a SearchResponse with records", searchResponse.getRecordCount() > 0);
	}
	
	/** Description: Tests that the advancedSearch returns valid SearchResponse records when refined by certification criteria AND cqms
	 * 
	 * Expected Result: Completes without error and returns some SearchResponse records
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_advancedSearch_refineByCertificationCriteriaAndCqms_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SearchRequest searchFilters = new SearchRequest();
		List<String> certificationCriteria = new ArrayList<String>();
		certificationCriteria.add("170.314 (a)(3)");
		List<String> cqms = new ArrayList<String>();
		cqms.add("0001");
		searchFilters.setCqms(cqms);
		searchFilters.setCertificationCriteria(certificationCriteria);
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setOrderBy("developer");
		searchFilters.setSortDescending(true);
		
		SearchResponse searchResponse = new SearchResponse();
		searchResponse = searchViewController.advancedSearch(searchFilters);
		assertTrue("searchViewController.simpleSearch() should return a SearchResponse with records", searchResponse.getRecordCount() > 0);
	}
	
	/** Description: Tests that the getPopulateSearchData(boolean required) method returns without error, 
	 * gets valid developerNames/productNames, 
	 * and returns within 3 seconds
	 * Expected Result: Completes without error, gets valid developerNames + productNames, and finishes within 3 seconds
	 * Assumptions:
	 * Pre-existing data in openchpl_test DB is there per the \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getPopulateSearchData_simpleAsTrue_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		long getPopulateSearchDataStartTime = System.currentTimeMillis();
		Boolean required = true;
		PopulateSearchOptions results = searchViewController.getPopulateSearchData(required);
		long getPopulateSearchDataEndTime = System.currentTimeMillis();
		long getPopulateSearchDataTimeLength = getPopulateSearchDataEndTime - getPopulateSearchDataStartTime;
		double getPopulateSearchElapsedSeconds = getPopulateSearchDataTimeLength / 1000.0;
		
		assertTrue("Returned " + results.getDeveloperNames().size() + " developers but should return more than 0", results.getDeveloperNames().size() > 0);
		assertTrue("Returned " + results.getProductNames().size() + " products but should return more than 0", results.getProductNames().size() > 0);
		
		System.out.println("searchViewController.getPopulateSearchData() should complete within 3 seconds. It took " + getPopulateSearchDataTimeLength
				+ " millis or " + getPopulateSearchElapsedSeconds + " seconds");
		
		// now try again to test caching
		getPopulateSearchDataStartTime = System.currentTimeMillis();
		required = true;
		results = searchViewController.getPopulateSearchData(required);
		getPopulateSearchDataEndTime = System.currentTimeMillis();
		getPopulateSearchDataTimeLength = getPopulateSearchDataEndTime - getPopulateSearchDataStartTime;
		getPopulateSearchElapsedSeconds = getPopulateSearchDataTimeLength / 1000.0;
		
		assertTrue("Returned " + results.getDeveloperNames().size() + " developers but should return more than 0", results.getDeveloperNames().size() > 0);
		assertTrue("Returned " + results.getProductNames().size() + " products but should return more than 0", results.getProductNames().size() > 0);
		
		System.out.println("searchViewController.getPopulateSearchData() should complete immediately due to caching. It took "
		+ getPopulateSearchDataTimeLength + " millis or " + getPopulateSearchElapsedSeconds + " seconds");
		assertTrue("DeveloperController.getDevelopers() should complete in 0 seconds due to caching", getPopulateSearchDataTimeLength < 100);
	}
}