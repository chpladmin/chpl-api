package gov.healthit.chpl.web.controller;

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
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class SearchViewControllerTest extends TestCase {
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
	@Test
	public void test_advancedSearch_refineByCqms_CompletesWithoutError() 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException {
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
	@Test
	public void test_advancedSearch_refineByCertificationCriteria_CompletesWithoutError() 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException {
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
	@Test
	public void test_advancedSearch_refineByCertificationCriteriaAndCqms_CompletesWithoutError() 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
			InvalidArgumentsException {
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
	
	/** 
	 * Tests that the getPopulateSearchOptions() caches its data
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getPopulateSearchOptions_CachesData() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		long startTime = System.currentTimeMillis();
		PopulateSearchOptions results = searchViewController.getPopulateSearchData(false);
		// getCertificationCriterionNumbers should now be cached
		long endTime = System.currentTimeMillis();
		long timeLength = endTime - startTime;
		double elapsedSecs = timeLength / 1000.0;
		
		assertTrue("Returned " + results.getCertBodyNames() + " getPopulateSearchOptions but should return more than 0", results.getCertBodyNames().size() > 0);
		
		System.out.println("getPopulateSearchOptions returned " + results.getCertBodyNames().size() + " total certBodyNames.");
		System.out.println("getPopulateSearchOptions returned " + results.getCertificationCriterionNumbers().size() + " total certificationCriterionNumbers.");
		System.out.println("getPopulateSearchOptions returned " + results.getCertificationStatuses().size() + " total certificationStatuses.");
		System.out.println("getPopulateSearchOptions returned " + results.getCqmCriterionNumbers().size() + " total cqmCriterionNumbers.");
		System.out.println("getPopulateSearchOptions returned " + results.getDeveloperNames().size() + " total developerNames.");
		System.out.println("getPopulateSearchOptions returned " + results.getEditions().size() + " total editions.");
		System.out.println("getPopulateSearchOptions returned " + results.getPracticeTypeNames().size() + " total practiceTypeNames.");
		System.out.println("getPopulateSearchOptions returned " + results.getProductClassifications().size() + " total productClassifications.");
		System.out.println("getPopulateSearchOptions returned " + results.getProductNames().size() + " total productNames.");
		System.out.println("getPopulateSearchOptions completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		// now compare cached time vs non-cached time
		startTime = System.currentTimeMillis();
		results = searchViewController.getPopulateSearchData(false);
		endTime = System.currentTimeMillis();
		timeLength = endTime - startTime;
		elapsedSecs = timeLength / 1000.0;
		System.out.println("getCertificationCriterionNumbers completed in  " + timeLength
				+ " millis or " + elapsedSecs + " seconds");
		
		assertTrue("getCertificationCriterionNumbers should complete within 100 ms but took " + timeLength
				+ " millis or " + elapsedSecs + " seconds", timeLength < 100);
	}
	
	/** 
	 * Tests that the getPopulateSearchData(true) caches its data
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getPopulateSearchData_simpleAsTrue_Caching_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
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
		results = searchViewController.getPopulateSearchData(required);
		getPopulateSearchDataEndTime = System.currentTimeMillis();
		getPopulateSearchDataTimeLength = getPopulateSearchDataEndTime - getPopulateSearchDataStartTime;
		getPopulateSearchElapsedSeconds = getPopulateSearchDataTimeLength / 1000.0;
		
		assertTrue("Returned " + results.getDeveloperNames().size() + " developers but should return more than 0", results.getDeveloperNames().size() > 0);
		assertTrue("Returned " + results.getProductNames().size() + " products but should return more than 0", results.getProductNames().size() > 0);
		
		System.out.println("searchViewController.getPopulateSearchData() should complete immediately due to caching. It took "
		+ getPopulateSearchDataTimeLength + " millis or " + getPopulateSearchElapsedSeconds + " seconds");
		assertTrue("searchViewController.getPopulateSearchData() should complete in 0 seconds due to caching but took " + getPopulateSearchDataTimeLength + "ms", getPopulateSearchDataTimeLength < 100);
	}
	
	/** 
	 * Tests that the getPopulateSearchData(false) caches its data
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getPopulateSearchData_simpleAsFalse_Caching_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		long getPopulateSearchDataStartTime = System.currentTimeMillis();
		Boolean required = false;
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
		results = searchViewController.getPopulateSearchData(required);
		getPopulateSearchDataEndTime = System.currentTimeMillis();
		getPopulateSearchDataTimeLength = getPopulateSearchDataEndTime - getPopulateSearchDataStartTime;
		getPopulateSearchElapsedSeconds = getPopulateSearchDataTimeLength / 1000.0;
		
		assertTrue("Returned " + results.getDeveloperNames().size() + " developers but should return more than 0", results.getDeveloperNames().size() > 0);
		assertTrue("Returned " + results.getProductNames().size() + " products but should return more than 0", results.getProductNames().size() > 0);
		
		System.out.println("searchViewController.getPopulateSearchData() should complete immediately due to caching. It took "
		+ getPopulateSearchDataTimeLength + " millis or " + getPopulateSearchElapsedSeconds + " seconds");
		assertTrue("searchViewController.getPopulateSearchData() should complete in 0 seconds due to caching but took " + getPopulateSearchDataTimeLength + "ms", getPopulateSearchDataTimeLength < 100);
	}
	
	/** 
	 * Given the CHPL is accepting search requests
	 * When I call the REST API's /decertifications/developers
	 * Then the controller method's getDecertifiedDevelopers returns expected results
	 */
	@Transactional
	@Test
	public void test_getDecertifiedDevelopers_CompletesWithoutError() throws EntityRetrievalException, JsonProcessingException, EntityCreationException{
		DecertifiedDeveloperResults resp = searchViewController.getDecertifiedDevelopers();
		assertTrue(resp.getDecertifiedDeveloperResults().size() > 0);
		assertTrue(resp.getDecertifiedDeveloperResults().get(0).getDeveloper() != null);
		assertTrue(resp.getDecertifiedDeveloperResults().get(0).getCertifyingBody() != null);
		Boolean hasNumMeaningfulUseNonNull = false;
		for(DecertifiedDeveloperResult ddr : resp.getDecertifiedDeveloperResults()){
			if(ddr.getEstimatedUsers() > 0){
				hasNumMeaningfulUseNonNull = true;
			}
		}
		assertTrue("DecertifiedDeveloperResults should contain an index with a non-null numMeaningfulUse.", hasNumMeaningfulUseNonNull);
	}
	
	/** 
	 * Given the CHPL is accepting search requests
	 * When I call the REST API's /
	 * Then the controller method's advancedSearch returns SearchResponse containing numMeaningfulUse
	 */
	@Transactional
	@Test
	public void test_advancedSearch_resultReturnsNumMeaningfulUse() throws InvalidArgumentsException {
		SearchRequest sr = new SearchRequest();
		sr.setPageNumber(0);
		sr.setPageSize(50);
		sr.setOrderBy("developer");
		sr.setSortDescending(true);
		sr.setDeveloper("VendorSuspended");
		
		SearchResponse resp = searchViewController.advancedSearch(sr);
		assertTrue("SearchResponse should have size > 0 but is " + resp.getResults().size(), resp.getResults().size() > 0);
		Boolean hasNumMeaningfulUse = false;
		for(CertifiedProductSearchResult result : resp.getResults()){
			if(result.getNumMeaningfulUse() != null){
				hasNumMeaningfulUse = true;
				break;
			}
		}
		assertTrue("SearchResponse should contain results with numMeaningfulUse.", hasNumMeaningfulUse);
	}
	
	/** 
	 * Given the CHPL is accepting search requests
	 * When I call the REST API's /
	 * Then the controller method's advancedSearch returns SearchResponse containing numMeaningfulUse
	 * @throws EntityRetrievalException 
	 */
	@Transactional
	@Test
	public void test_simpleSearch_resultReturnsNumMeaningfulUse() throws EntityRetrievalException {
		String searchTerm = "VendorSuspended";
		Integer pageNumber = 0;
		Integer pageSize = 50;
		String orderBy = "developer";
		Boolean sortDescending = true;
		
		SearchResponse resp = searchViewController.simpleSearch(searchTerm, pageNumber, pageSize, orderBy, sortDescending);
		assertTrue("SearchResponse should have size > 0 but is " + resp.getResults().size(), resp.getResults().size() > 0);
		Boolean hasNumMeaningfulUse = false;
		for(CertifiedProductSearchResult result : resp.getResults()){
			if(result.getNumMeaningfulUse() != null){
				hasNumMeaningfulUse = true;
				break;
			}
		}
	assertTrue("SearchResponse should contain results with numMeaningfulUse.", hasNumMeaningfulUse);
	}
	
	/** 
	 * Given the CHPL is accepting search requests
	 * When I call the REST API's /
	 * Then the controller method's getCertifiedProductDetails returns CertifiedProductSearchDetails containing numMeaningfulUse
	 * @throws EntityRetrievalException 
	 */
	@Transactional
	@Test
	public void test_getCertifiedProductDetails_resultReturnsNumMeaningfulUse() throws EntityRetrievalException {
		Long cpId = 6L;
		
		CertifiedProductSearchDetails resp = searchViewController.getCertifiedProductDetails(cpId);
		assertTrue("Response should contain results but is null", resp != null);
		assertTrue("Response should contain certified product with numMeaningfulUse == 12 but contains numMeaningfulUse of " + resp.getNumMeaningfulUse(),
				resp.getNumMeaningfulUse() == 12);
	}
	
	/** 
	 * Given that a user with no security calls the API
	 * When the API is called at /decertifications/certified_products
	 * Then the API returns a SearchResponse object with only decertified CPs
	 * Then the pageSize is equivalent to the sum of all the decertified CPs
	 * @throws EntityRetrievalException 
	 */
	@Transactional
	@Test
	public void test_searchDecertifiedCPs() throws EntityRetrievalException {	
		SearchResponse resp = searchViewController.getDecertifiedCertifiedProducts(null, null, null, null);		
		
		assertTrue(resp.getResults().size() == 1);
		assertEquals((Integer) resp.getResults().size(), resp.getPageSize());
		for(CertifiedProductSearchResult cp : resp.getResults()){
			assertTrue(cp.getCertificationStatus().containsValue(String.valueOf(CertificationStatusType.WithdrawnByAcb)) || 
					cp.getCertificationStatus().containsValue(String.valueOf(CertificationStatusType.WithdrawnByDeveloperUnderReview)) ||
					cp.getCertificationStatus().containsValue(String.valueOf(CertificationStatusType.TerminatedByOnc)));
		}
	}
	
	/** 
	 * Given that a user with no security calls the API
	 * When the API is called at /decertifications/certified_products
	 * Then the API returns a SearchResponse object with only decertified inactive certificate CPs
	 * Then the pageSize is equivalent to the sum of all the decertified CPs
	 * @throws EntityRetrievalException 
	 */
	@Transactional
	@Test
	public void test_searchDecertifiedInactiveCertCPs() throws EntityRetrievalException {	
		SearchResponse resp = searchViewController.getDecertifiedInactiveCertificateCertifiedProducts(null, null, null, null);		
		
		assertTrue(resp.getResults().size() == 6);
		assertEquals((Integer) resp.getResults().size(), resp.getPageSize());
		for(CertifiedProductSearchResult cp : resp.getResults()){
			assertTrue(cp.getCertificationStatus().containsValue(String.valueOf(CertificationStatusType.WithdrawnByDeveloper)));
		}
	}
}